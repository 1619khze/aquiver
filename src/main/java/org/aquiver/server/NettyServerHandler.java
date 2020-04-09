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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.BeanManager;
import org.aquiver.Request;
import org.aquiver.mvc.ArgsConverter;
import org.aquiver.mvc.LogicExecutionWrapper;
import org.aquiver.mvc.RequestHandler;
import org.aquiver.mvc.RequestHandlerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

  private Map<String, RequestHandler> requestHandlers;
  private List<ArgsConverter<?>>      argsConverters;

  public NettyServerHandler(BeanManager beanManager) {
    this.requestHandlers = beanManager.getMappingRegistry().getRequestHandlers();
    this.argsConverters  = beanManager.getMappingRegistry().getArgsConverters();
  }

  /**
   * Returns if the future has successfully completed.
   */
  private boolean isReady(CompletableFuture<?> future) {
    return (future != null) && future.isDone()
            && !future.isCompletedExceptionally()
            && (future.join() != null);
  }

  /**
   * Returns the current value or null if either not done or failed.
   */
  @SuppressWarnings("NullAway")
  private <V> V getIfReady(CompletableFuture<V> future) {
    return isReady(future) ? future.join() : null;
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
    CompletableFuture<FullHttpRequest> future = CompletableFuture.completedFuture(fullHttpRequest);
    future.thenApply(this::thenApplyHttpEntity)
            .thenApply(this::thenExecutionLogic)
            .thenApply(result -> this.writeResponse(result, fullHttpRequest, ctx))
            .whenComplete((refresh, throwable) -> refreshResponse(ctx, refresh));
  }

  private void refreshResponse(ChannelHandlerContext ctx, Boolean refresh) {
    if (!refresh) {
      ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
  }

  private Request thenApplyHttpEntity(FullHttpRequest request) {
    return new Request(request);
  }

  private Object thenExecutionLogic(Request request) {
    CompletableFuture<Request> lookUpaFuture = CompletableFuture.completedFuture(request);
    CompletableFuture<Object> resultFuture = lookUpaFuture.thenApplyAsync(this::lookupRequestHandler)
            .thenApply(requestHandler -> this.assignmentParameters(requestHandler, request))
            .thenApply(this::executionLogic);
    return getIfReady(resultFuture);
  }

  private Object executionLogic(LogicExecutionWrapper logicExecutionWrapper) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      RequestHandler requestHandler = logicExecutionWrapper.getRequestHandler();
      Method method = requestHandler.getClazz()
              .getMethod(requestHandler.getMethod(), logicExecutionWrapper.getParamTypes());
      MethodHandle methodHandle = lookup.unreflect(method);
      return methodHandle.bindTo(requestHandler.getBean()).invokeWithArguments(logicExecutionWrapper.getParamValues());
    } catch (Throwable throwable) {
      log.error("An exception occurred when calling the mapping method", throwable);
    }
    return null;
  }

  private RequestHandler lookupRequestHandler(Request request) {
    String lookupPath = request.getUri().endsWith("/")
            ? request.getUri().substring(0, request.getUri().length() - 1)
            : request.getUri();
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }
    return requestHandlers.get(lookupPath);
  }

  private LogicExecutionWrapper assignmentParameters(RequestHandler requestHandler, Request request) {
    List<RequestHandlerParam> params = requestHandler.getParams();

    Object[]   paramValues = new Object[params.size()];
    Class<?>[] paramTypes  = new Class[params.size()];

    for (int i = 0; i < paramValues.length; i++) {
      RequestHandlerParam handlerParam = requestHandler.getParams().get(i);
      paramTypes[i]  = handlerParam.getDataType();
      paramValues[i] = request.assignment(handlerParam);
    }
    return new LogicExecutionWrapper(requestHandler, paramValues, paramTypes);
  }

  private boolean writeResponse(Object result, FullHttpRequest request, ChannelHandlerContext ctx) {
    boolean keepAlive = HttpUtil.isKeepAlive(request);
    FullHttpResponse response = new DefaultFullHttpResponse(
            HTTP_1_1, request.decoderResult().isSuccess() ? OK : BAD_REQUEST,
            Unpooled.copiedBuffer(Objects.isNull(result) ? "".getBytes(CharsetUtil.UTF_8) : result.toString().getBytes(CharsetUtil.UTF_8)));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    if (keepAlive) {
      response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }
    ctx.write(response);
    return keepAlive;
  }
}
