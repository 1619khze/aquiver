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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.EventExecutor;
import org.aquiver.RequestContext;
import org.aquiver.mvc.render.ResponseRenderMatcher;
import org.aquiver.mvc.route.PathRouteMatcher;
import org.aquiver.mvc.route.RouteContext;
import org.aquiver.mvc.route.RouteMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author WangYi
 * @since 2020/5/26
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

  private final static String FAVICON_PATH = "/favicon.ico";
  private final RouteMatcher<RequestContext> matcher;
  private final ResponseRenderMatcher responseRenderMatcher;

  public NettyServerHandler(RouteContext routeContext) {
    this.matcher = new PathRouteMatcher(routeContext.getRoutes());
    this.responseRenderMatcher = new ResponseRenderMatcher(routeContext.getAquiver());
  }

  @Override
  public boolean acceptInboundMessage(final Object msg) {
    return msg instanceof FullHttpRequest;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("HttpRequestHandler error", cause);
    ctx.close();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
    if (FAVICON_PATH.equals(fullHttpRequest.uri())) {
      return;
    }

    CompletableFuture<FullHttpRequest> future = CompletableFuture.completedFuture(fullHttpRequest);
    EventExecutor executor = ctx.executor();

    future.thenApply(request -> this.thenApplyRequestContext(request, ctx))
            .thenApplyAsync(this.matcher::match, executor)
            .thenAcceptAsync(this::writeResponse, executor);
  }

  private RequestContext thenApplyRequestContext(FullHttpRequest request, ChannelHandlerContext ctx) {
    return new RequestContext(request, ctx);
  }

  private void writeResponse(RequestContext requestContext) {
    this.responseRenderMatcher.adapter(requestContext);
  }
}
