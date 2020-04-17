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
import org.aquiver.RequestContext;
import org.aquiver.mvc.LogicExecutionWrapper;
import org.aquiver.mvc.ParameterDispenser;
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

  private final Map<String, RequestHandler> requestHandlers;

  public NettyServerHandler(BeanManager beanManager) {
    this.requestHandlers = beanManager.getMappingRegistry().getRequestHandlers();
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
            .whenComplete((refresh, throwable) -> refreshResponse(ctx, refresh, throwable));
  }

  private void refreshResponse(ChannelHandlerContext ctx, Boolean refresh, Throwable throwable) {
    if (!refresh) {
      ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
  }

  private RequestContext thenApplyHttpEntity(FullHttpRequest request) {
    return new RequestContext(request);
  }

  private Object thenExecutionLogic(RequestContext requestContext) {
    CompletableFuture<RequestContext> lookUpaFuture = CompletableFuture.completedFuture(requestContext);
    CompletableFuture<Object> resultFuture = lookUpaFuture.thenApplyAsync(this::lookupRequestHandler)
            .thenApply(requestHandler -> this.assignmentParameters(requestHandler, requestContext))
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

  private RequestHandler lookupRequestHandler(RequestContext requestContext) {
    String lookupPath = requestContext.getUri().endsWith("/")
            ? requestContext.getUri().substring(0, requestContext.getUri().length() - 1)
            : requestContext.getUri();
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }

    if (requestHandlers == null || requestHandlers.isEmpty()) {
      return null;
    }

    if (requestHandlers.containsKey(lookupPath)) {
      return this.requestHandlers.get(lookupPath);
    }

    for (Map.Entry<String, RequestHandler> entry : requestHandlers.entrySet()) {
      String matcher = this.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher)) {
        return null;
      }
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      if (lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }
      if (checkMatch(lookupPathSplit, mappingUrlSplit)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private boolean checkMatch(String[] lookupPathSplit, String... mappingUrlSplit) {
    for (int i = 0; i < lookupPathSplit.length; i++) {
      if (lookupPathSplit[i].equals(mappingUrlSplit[i])) {
        continue;
      }
      if (!mappingUrlSplit[i].startsWith("{")) {
        return false;
      }
    }
    return true;
  }

  private String getMatch(String url) {
    StringBuilder matcher = new StringBuilder(128);
    for (char c : url.toCharArray()) {
      if (c == '{') {
        break;
      }
      matcher.append(c);
    }
    return matcher.toString();
  }

  private LogicExecutionWrapper assignmentParameters(RequestHandler requestHandler, RequestContext requestContext) {
    List<RequestHandlerParam> params = requestHandler.getParams();

    Object[]   paramValues = new Object[params.size()];
    Class<?>[] paramTypes  = new Class[params.size()];

    for (int i = 0; i < paramValues.length; i++) {
      RequestHandlerParam handlerParam = requestHandler.getParams().get(i);
      paramTypes[i]  = handlerParam.getDataType();
      paramValues[i] = ParameterDispenser.dispen(handlerParam, requestContext, requestHandler.getUrl());
    }
    return new LogicExecutionWrapper(requestHandler, paramValues, paramTypes);
  }

  private boolean writeResponse(Object result, FullHttpRequest request, ChannelHandlerContext ctx) {
    boolean keepAlive = HttpUtil.isKeepAlive(request);
    FullHttpResponse response = new DefaultFullHttpResponse(
            HTTP_1_1, request.decoderResult().isSuccess() ? OK : BAD_REQUEST,
            Unpooled.copiedBuffer(Objects.isNull(result) ? "".getBytes(CharsetUtil.UTF_8) :
                    result.toString().getBytes(CharsetUtil.UTF_8)));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    if (keepAlive) {
      response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }
    ctx.write(response);
    return keepAlive;
  }
}
