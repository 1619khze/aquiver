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
package org.aquiver;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.aquiver.mvc.router.RouteInfo;

/**
 * @author WangYi
 * @since 2020/6/27
 */
public class RequestContext {
  private Throwable throwable;
  private RouteInfo routeInfo;
  private final Request request;
  private final Response response;

  public RequestContext(FullHttpRequest httpRequest, ChannelHandlerContext context) {
    this.request = new Request(httpRequest, context);
    this.response = new Response();
  }

  public RouteInfo route() {
    return routeInfo;
  }

  public void route(RouteInfo routeInfo) {
    this.routeInfo = routeInfo;
  }

  public Request request() {
    return request;
  }

  public Response response() {
    return response;
  }

  public Throwable throwable() {
    return throwable;
  }

  public void throwable(Throwable throwable) {
    this.throwable = throwable;
  }

  public void redirect(String redirectUrl) {
    final HttpHeaders headers = new DefaultHttpHeaders();
    headers.set(HttpHeaderNames.LOCATION, redirectUrl);

    final FullHttpResponse response = ResultResponseBuilder.forResponse(
            HttpResponseStatus.PERMANENT_REDIRECT).headers(headers).build();

    this.writeAndFlush(response);
  }

  public void writeAndFlush(FullHttpResponse fullHttpResponse) {
    request().channelHandlerContext().writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
  }
}
