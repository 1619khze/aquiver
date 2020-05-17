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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.Response;
import org.aquiver.RequestMappingResolver;
import org.aquiver.mvc.LogicExecutionResponse;
import org.aquiver.mvc.ParameterDispenser;
import org.aquiver.mvc.RequestHandler;
import org.aquiver.mvc.RequestHandlerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.mvc.MediaType.APPLICATION_JSON_VALUE;
import static org.aquiver.mvc.MediaType.TEXT_PLAIN_VALUE;

public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

  private final static String FAVICON_PATH = "/favicon.ico";
  private final static String EMPTY_STRING = "";

  private final Map<String, RequestHandler> requestHandlers;

  public NettyServerHandler(RequestMappingResolver requestMappingResolver) {
    this.requestHandlers = requestMappingResolver.getMappingRegistry().getRequestHandlers();
  }

  @Override
  public boolean acceptInboundMessage(final Object msg) {
    return msg instanceof FullHttpRequest;
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
    if (FAVICON_PATH.equals(fullHttpRequest.uri())) {
      return;
    }

    CompletableFuture<FullHttpRequest> future = CompletableFuture.completedFuture(fullHttpRequest);
    ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    future.thenApply(request -> this.thenApplyRequestContext(request, ctx))
            .thenApplyAsync(this::thenExecutionLogic, forkJoinPool)
            .thenAcceptAsync(this::writeResponse, forkJoinPool);
    future.complete(fullHttpRequest);
  }

  private RequestContext thenApplyRequestContext(FullHttpRequest request, ChannelHandlerContext channelHandlerContext) {
    return new RequestContext(request, channelHandlerContext);
  }

  private RequestContext thenExecutionLogic(RequestContext requestContext) {
    CompletableFuture<RequestContext> lookUpaFuture = CompletableFuture.completedFuture(requestContext);
    CompletableFuture<Response> resultFuture = lookUpaFuture
            .thenApply(this::lookupRequestHandler)
            .thenApply(this::invokeParam)
            .thenApply(this::executionLogic);
    Response ifReady = getIfReady(resultFuture);
    requestContext.setResponse(ifReady);
    resultFuture.complete(ifReady);
    return requestContext;
  }

  private Response executionLogic(LogicExecutionResponse logicExecutionResponse) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      RequestHandler requestHandler = logicExecutionResponse.getRequestHandler();
      Method method = requestHandler.getClazz()
              .getMethod(requestHandler.getMethod(),
                      logicExecutionResponse.getParamTypes());
      MethodHandle methodHandle = lookup.unreflect(method);
      Object result = methodHandle.bindTo(requestHandler.getBean())
              .invokeWithArguments(logicExecutionResponse.getParamValues());
      return new Response(result, requestHandler.isJsonResponse());
    } catch (Throwable throwable) {
      log.error("An exception occurred when calling the mapping method", throwable);
    }
    return null;
  }

  private RequestContext lookupRequestHandler(RequestContext requestContext) {
    String lookupPath = requestContext.getUri().endsWith("/")
            ? requestContext.getUri().substring(0, requestContext.getUri().length() - 1)
            : requestContext.getUri();
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }
    if (preLookUp(requestContext, lookupPath)) {
      return requestContext;
    }
    if (loopLookUp(requestContext, lookupPath)) {
      return requestContext;
    }
    this.handleException(new RuntimeException("There is no corresponding " +
            "request mapping program:" + lookupPath), requestContext);
    return requestContext;
  }

  private boolean loopLookUp(RequestContext requestContext, String lookupPath) {
    for (Map.Entry<String, RequestHandler> entry : requestHandlers.entrySet()) {
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      String matcher = this.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher) || lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }

      if (checkMatch(lookupPathSplit, mappingUrlSplit)) {
        RequestHandler value = entry.getValue();
        requestContext.setRequestHandler(value);
        return true;
      }
    }
    return false;
  }

  private boolean preLookUp(RequestContext requestContext, String lookupPath) {
    if (requestHandlers == null || requestHandlers.isEmpty()) {
      throw new RuntimeException("There is no corresponding request mapping program:" + lookupPath);
    }

    if (requestHandlers.containsKey(lookupPath)) {
      RequestHandler handler = this.requestHandlers.get(lookupPath);
      requestContext.setRequestHandler(handler);
      return true;
    }
    return false;
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

  private LogicExecutionResponse invokeParam(RequestContext requestContext) {
    RequestHandler requestHandler = requestContext.getRequestHandler();
    List<RequestHandlerParam> params = requestHandler.getParams();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];

    for (int i = 0; i < paramValues.length; i++) {
      RequestHandlerParam handlerParam = requestHandler.getParams().get(i);
      paramTypes[i] = handlerParam.getDataType();
      paramValues[i] = ParameterDispenser.dispen(handlerParam, requestContext, requestHandler.getUrl());
    }
    return LogicExecutionResponse.of(requestHandler, paramValues, paramTypes);
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

  protected void handleException(Exception e, RequestContext requestContext) {
    log.warn("There is no corresponding request mapping program: {}", requestContext.getUri());
    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
            HTTP_1_1, INTERNAL_SERVER_ERROR,
            Unpooled.copiedBuffer(e.getMessage().getBytes(StandardCharsets.UTF_8))
    );
    ChannelFuture channelFuture = requestContext.getContext().writeAndFlush(fullHttpResponse);
    channelFuture.addListener(ChannelFutureListener.CLOSE);
  }
}
