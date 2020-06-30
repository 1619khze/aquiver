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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import org.aquiver.RequestContext;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/5/28
 */
public class StaticFileServerHandler {
  private static void send100Continue(ChannelHandlerContext ctx) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
    ctx.writeAndFlush(response);
  }

  public Boolean handle(RequestContext requestContext) throws Exception {
    FullHttpRequest request = requestContext.request().httpRequest();
    ChannelHandlerContext ctx = requestContext.request().channelHandlerContext();
    String uri = request.uri();
    if ("favicon.ico".equals(uri)) {
      return false;
    }
    URL resource = this.getClass().getClassLoader().getResource(uri.replaceFirst("/", ""));
    URL notFoundUrl = this.getClass().getClassLoader().getResource("404.html");

    if(Objects.isNull(resource)){
      return false;
    }

    File html = new File(resource.toURI());
    File NOT_FOUND = new File(notFoundUrl.toURI());

    if (HttpUtil.is100ContinueExpected(request)) {
      send100Continue(ctx);
    }

    if (!html.exists()) {
      html = NOT_FOUND;
    }
    try (RandomAccessFile file = new RandomAccessFile(html, "r")) {
      HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

      if (html == NOT_FOUND) {
        response.setStatus(HttpResponseStatus.NOT_FOUND);
      }

      if (uri.endsWith(".html")) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
      } else if (uri.endsWith(".js")) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
      } else if (uri.endsWith(".css")) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
      }

      boolean keepAlive = HttpUtil.isKeepAlive(request);

      if (keepAlive) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      }
      ctx.write(response);

      if (ctx.pipeline().get(SslHandler.class) == null) {
        ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
      } else {
        ctx.write(new ChunkedNioFile(file.getChannel()));
      }

      ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
      if (!keepAlive) {
        future.addListener(ChannelFutureListener.CLOSE);
      }
    }

    return true;
  }
}
