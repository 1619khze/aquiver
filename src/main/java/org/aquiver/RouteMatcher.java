package org.aquiver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import org.aquiver.mvc.ParameterDispenser;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.RouteInvokeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author WangYi
 * @since 2020/5/26
 */
public final class RouteMatcher {
  private static final Logger log = LoggerFactory.getLogger(RouteMatcher.class);

  private final Map<String, Route> routeMap;

  public RouteMatcher(Map<String, Route> routeMap) {
    this.routeMap = routeMap;
  }

  /** Returns if the future has successfully completed. */
  private boolean isReady(CompletableFuture<?> future) {
    return (future != null) && future.isDone()
            && !future.isCompletedExceptionally()
            && (future.join() != null);
  }

  /** Returns the current value or null if either not done or failed. */
  @SuppressWarnings("NullAway")
  private <V> V getIfReady(CompletableFuture<V> future) {
    return isReady(future) ? future.join() : null;
  }

  public RequestContext thenExecutionLogic(RequestContext requestContext) {
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

  private RouteInvokeWrapper invokeParam(RequestContext requestContext) {
    Route route = requestContext.getRoute();
    List<RequestHandlerParam> params = route.getParams();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];

    for (int i = 0; i < paramValues.length; i++) {
      RequestHandlerParam handlerParam = route.getParams().get(i);
      paramTypes[i] = handlerParam.getDataType();
      paramValues[i] = ParameterDispenser.dispen(handlerParam, requestContext, route.getUrl());
    }
    return RouteInvokeWrapper.of(route, paramValues, paramTypes);
  }

  private Response executionLogic(RouteInvokeWrapper routeInvokeWrapper) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      Route route = routeInvokeWrapper.getRoute();
      Method method = route.getClazz()
              .getMethod(route.getMethod(),
                      routeInvokeWrapper.getParamTypes());
      MethodHandle methodHandle = lookup.unreflect(method);
      Object result = methodHandle.bindTo(route.getBean())
              .invokeWithArguments(routeInvokeWrapper.getParamValues());
      return new Response(result, route.isJsonResponse());
    } catch (Throwable throwable) {
      log.error("An exception occurred when calling the mapping method", throwable);
    }
    return null;
  }

  private boolean loopLookUp(RequestContext requestContext, String lookupPath) {
    for (Map.Entry<String, Route> entry : routeMap.entrySet()) {
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      String matcher = this.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher) || lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }

      if (checkMatch(lookupPathSplit, mappingUrlSplit)) {
        Route value = entry.getValue();
        requestContext.setRoute(value);
        return true;
      }
    }
    return false;
  }

  private boolean preLookUp(RequestContext requestContext, String lookupPath) {
    if (routeMap == null || routeMap.isEmpty()) {
      throw new RuntimeException("There is no corresponding request mapping program:" + lookupPath);
    }

    if (routeMap.containsKey(lookupPath)) {
      Route handler = this.routeMap.get(lookupPath);
      requestContext.setRoute(handler);
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
