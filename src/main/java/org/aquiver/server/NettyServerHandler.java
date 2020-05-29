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

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.mvc.MediaType.APPLICATION_JSON_VALUE;
import static org.aquiver.mvc.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author WangYi
 * @since 2020/5/26
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

  private final static String FAVICON_PATH = "/favicon.ico";
  private final static String EMPTY_STRING = "";
  private final RouteMatcher<RequestContext> matcher;

  public NettyServerHandler(RouteContext routeContext) {
    this.matcher = new PathRouteMatcher(routeContext.getRoutes());
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
    ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    future.thenApply(request -> this.thenApplyRequestContext(request, ctx))
            .thenApplyAsync(this.matcher::match, forkJoinPool)
            .thenAcceptAsync(this::writeResponse, forkJoinPool);
    future.complete(fullHttpRequest);
  }

  private RequestContext thenApplyRequestContext(FullHttpRequest request, ChannelHandlerContext ctx) {
    return new RequestContext(request, ctx);
  }

  private void writeResponse(RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    Response response = requestContext.getResponse();
    Object result = response.getResult();
    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
            HTTP_1_1, httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST,
            Unpooled.copiedBuffer(Objects.isNull(result)
                    ? EMPTY_STRING.getBytes(CharsetUtil.UTF_8)
                    : response.isJsonResponse()
                    ? JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8)
                    : result.toString().getBytes(StandardCharsets.UTF_8)
            )
    );
    HttpHeaders headers = fullHttpResponse.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, response.isJsonResponse() ? APPLICATION_JSON_VALUE : TEXT_PLAIN_VALUE);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
    }
    requestContext.getContext().writeAndFlush(fullHttpResponse);
  }
}
