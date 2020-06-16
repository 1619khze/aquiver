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
import org.aquiver.RequestHandler;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * @author WangYi
 * @since 2020/5/28
 */
public class StaticFileServerHandler implements RequestHandler<Boolean> {
  @Override
  public Boolean handle(RequestContext requestContext) throws Exception {
    FullHttpRequest request = requestContext.getHttpRequest();
    ChannelHandlerContext ctx = requestContext.getContext();
    // 获取URI
    String uri = request.uri();
    // 设置不支持favicon.ico文件
    if ("favicon.ico".equals(uri)) {
      return false;
    }
    // 根据路径地址构建文件
    URL resource = this.getClass().getClassLoader().getResource(uri.replaceFirst("/", ""));
    URL notFoundUrl = this.getClass().getClassLoader().getResource("404.html");

    File html = new File(resource.toURI());
    File NOT_FOUND = new File(notFoundUrl.toURI());

    // 状态为1xx的话，继续请求
    if (HttpUtil.is100ContinueExpected(request)) {
      send100Continue(ctx);
    }

    // 当文件不存在的时候，将资源指向NOT_FOUND
    if (!html.exists()) {
      html = NOT_FOUND;
    }

    RandomAccessFile file = new RandomAccessFile(html, "r");
    HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);

    // 文件没有发现设置状态为404
    if (html == NOT_FOUND) {
      response.setStatus(HttpResponseStatus.NOT_FOUND);
    }

    // 设置文件格式内容
    if (uri.endsWith(".html")) {
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
    } else if (uri.endsWith(".js")) {
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/x-javascript");
    } else if (uri.endsWith(".css")) {
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/css; charset=UTF-8");
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
    // 写入文件尾部
    ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
    file.close();

    return true;
  }

  private static void send100Continue(ChannelHandlerContext ctx) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
    ctx.writeAndFlush(response);
  }
}
