/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author WangYi
 * @since 2020/5/28
 */
public class StaticFileServerHandler implements RequestHandler {
  private static final Logger log = LoggerFactory.getLogger(StaticFileServerHandler.class);

  public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
  public static final int HTTP_CACHE_SECONDS = 60;

  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
  private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-._]?[^<>&\"]*");

  private FullHttpRequest request;

  @Override
  public void handle(RequestContext requestContext) throws Exception {
    this.request = requestContext.getHttpRequest();
    ChannelHandlerContext ctx = requestContext.getContext();

    /** if the decoder result is fail, it return the http status that the bad request . */
    if (!request.decoderResult().isSuccess()) {
      sendError(ctx, BAD_REQUEST);
      return;
    }

    /** If the request method is not obtained, it returns the http status that the method does not allowed. */
    if (!GET.equals(request.method())) {
      this.sendError(ctx, METHOD_NOT_ALLOWED);
      return;
    }

    /** if the access file path is forbidden, it returns the http status that the method does forbidden. */
    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    String fileUri = request.uri();

    if (INSECURE_URI.matcher(fileUri).matches() || !ALLOWED_FILE_NAME.matcher(fileUri).matches()) {
      sendError(ctx, FORBIDDEN);
    }

    if (fileUri.startsWith("/")) {
      fileUri = fileUri.replaceFirst("/", "");
    }

    URL url = this.getClass().getClassLoader().getResource(fileUri);
    if (Objects.isNull(url)) {
      this.sendError(ctx, NOT_FOUND);
      return;
    }

    Path filePath = Paths.get(url.toURI());
    if (Files.isHidden(filePath) || !Files.exists(filePath)) {
      this.sendError(ctx, NOT_FOUND);
      return;
    }

    File file = filePath.toFile();

    if (Files.isDirectory(filePath) || !Files.isRegularFile(filePath)) {
      sendError(ctx, FORBIDDEN);
      return;
    }

    // Cache Validation
    String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
    if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
      SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
      Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

      // Only compare up to the second because the datetime format we send to the client
      // does not have milliseconds
      long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
      long fileLastModifiedSeconds = file.lastModified() / 1000;
      if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
        this.sendNotModified(ctx);
        return;
      }
    }

    RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, "r");
    } catch (FileNotFoundException ignore) {
      sendError(ctx, NOT_FOUND);
      return;
    }
    long fileLength = raf.length();

    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    HttpUtil.setContentLength(response, fileLength);
    setContentTypeHeader(response, file);
    setDateAndCacheHeaders(response, file);

    if (!keepAlive) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    } else if (request.protocolVersion().equals(HTTP_1_0)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    // Write the initial line and the header.
    ctx.write(response);

    // Write the content.
    ChannelFuture sendFileFuture;
    ChannelFuture lastContentFuture;
    if (ctx.pipeline().get(SslHandler.class) == null) {
      sendFileFuture =
              ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
      // Write the end marker.
      lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    } else {
      sendFileFuture =
              ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                      ctx.newProgressivePromise());
      // HttpChunkedInput will write the end marker (LastHttpContent) for us.
      lastContentFuture = sendFileFuture;
    }

    sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
      @Override
      public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
        if (total < 0) { // total unknown
          log.info(future.channel() + " Transfer progress: " + progress);
        } else {
          log.info(future.channel() + " Transfer progress: " + progress + " / " + total);
        }
      }

      @Override
      public void operationComplete(ChannelProgressiveFuture future) {
        log.info(future.channel() + " Transfer complete.");
      }
    });
    // Decide whether to close the connection or not.
    if (!keepAlive) {
      // Close the connection when the whole content is written out.
      lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    FullHttpResponse response = new DefaultFullHttpResponse(
            HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    this.sendAndCleanupConnection(ctx, response);
  }

  /**
   * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
   *
   * @param ctx Context
   */
  private void sendNotModified(ChannelHandlerContext ctx) {
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
    setDateHeader(response);

    this.sendAndCleanupConnection(ctx, response);
  }

  /**
   * If Keep-Alive is disabled, attaches "Connection: close" header to the response
   * and closes the connection after the response being sent.
   */
  private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response) {
    final FullHttpRequest request = this.request;
    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    HttpUtil.setContentLength(response, response.content().readableBytes());
    if (!keepAlive) {
      // We're going to close the connection as soon as the response is sent,
      // so we should also make it clear for the client.
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    } else if (request.protocolVersion().equals(HTTP_1_0)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    ChannelFuture flushPromise = ctx.writeAndFlush(response);

    if (!keepAlive) {
      // Close the connection as soon as the response is sent.
      flushPromise.addListener(ChannelFutureListener.CLOSE);
    }
  }

  /**
   * Sets the Date header for the HTTP response
   *
   * @param response HTTP response
   */
  private void setDateHeader(FullHttpResponse response) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    Calendar time = new GregorianCalendar();
    response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
  }

  /**
   * Sets the Date and Cache headers for the HTTP Response
   *
   * @param response    HTTP response
   * @param fileToCache file to extract content type
   */
  private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    // Date header
    Calendar time = new GregorianCalendar();
    response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

    // Add cache headers
    time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
    response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
    response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
  }

  /**
   * Sets the content type header for the HTTP Response
   *
   * @param response HTTP response
   * @param file     file to extract content type
   */
  private void setContentTypeHeader(HttpResponse response, File file) {
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, file.getPath());
  }
}
