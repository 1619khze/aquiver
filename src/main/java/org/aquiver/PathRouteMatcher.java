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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.aquiver.handler.HttpExceptionHandler;
import org.aquiver.mvc.ParameterDispenser;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.RouteInvokeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author WangYi
 * @since 2020/5/29
 */
public class PathRouteMatcher implements RouteMatcher<RequestContext> {
  private static final Logger log = LoggerFactory.getLogger(RouteMatcher.class);

  private final Map<String, Route> routeMap;
  private final HttpExceptionHandler exceptionHandler;
  private final FileRouteMatcher fileRouteMatcher;

  public PathRouteMatcher(Map<String, Route> routeMap) {
    this.routeMap = routeMap;
    this.exceptionHandler = new HttpExceptionHandler();
    this.fileRouteMatcher = new FileRouteMatcher();
  }

  @Override
  public RequestContext match(RequestContext context) {
    CompletableFuture<RequestContext> lookUpaFuture =
            CompletableFuture.completedFuture(context);

    CompletableFuture<Response> resultFuture = lookUpaFuture
            .thenApply(this::lookupRoute)
            .thenApply(this::invokeParam)
            .thenApply(this::invokeMethod);
    Response response = Async.getIfReady(resultFuture);
    context.setResponse(response);
    resultFuture.complete(response);
    return context;
  }

  private RouteInvokeWrapper invokeParam(RequestContext context) {
    Route route = context.getRoute();
    List<RequestHandlerParam> params = route.getParams();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];

    for (int i = 0; i < paramValues.length; i++) {
      RequestHandlerParam handlerParam = route.getParams().get(i);
      paramTypes[i] = handlerParam.getDataType();
      paramValues[i] = ParameterDispenser.dispen(
              handlerParam, context, route.getUrl()
      );
    }
    return RouteInvokeWrapper.of(route, paramValues, paramTypes);
  }

  private Response invokeMethod(RouteInvokeWrapper wrapper) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      Route route = wrapper.getRoute();
      Method method = this.getInvokeMethod(wrapper, route);

      Object result = lookup.unreflect(method).bindTo(route.getBean())
              .invokeWithArguments(wrapper.getParamValues());

      return new Response(result, route.isJsonResponse());
    } catch (Throwable throwable) {
      log.error("An exception occurred when calling the mapping method", throwable);
    }
    return null;
  }

  private Method getInvokeMethod(RouteInvokeWrapper wrapper, Route route) throws NoSuchMethodException {
    return route.getClazz().getMethod(route.getMethod(), wrapper.getParamTypes());
  }

  private boolean loopLookUp(RequestContext context, String lookupPath) {
    for (Map.Entry<String, Route> entry : routeMap.entrySet()) {
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      String matcher = PathVarMatcher.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher) || lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }
      if (PathVarMatcher.checkMatch(lookupPathSplit, mappingUrlSplit)) {
        Route value = entry.getValue();
        context.setRoute(value);
        return true;
      }
    }
    return false;
  }

  private boolean preLookUp(RequestContext context, String lookupPath) {
    if (routeMap.containsKey(lookupPath)) {
      Route handler = this.routeMap.get(lookupPath);
      context.setRoute(handler);
      return true;
    } else {
      return false;
    }
  }

  private RequestContext lookupRoute(RequestContext context) {
    String lookupPath = context.getUri().endsWith("/")
            ? context.getUri().substring(0, context.getUri().length() - 1)
            : context.getUri();
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }
    if (routeMap == null || routeMap.isEmpty()) {
      fileMatch(context);
    }
    if (preLookUp(context, lookupPath)) {
      return context;
    }
    if (loopLookUp(context, lookupPath)) {
      return context;
    }
    fileMatch(context);
    return context;
  }

  private void fileMatch(RequestContext context) {
    if (!fileRouteMatcher.match(context)) {
      handlerNoRouteFoundException(context);
    }
  }

  private void handlerNoRouteFoundException(RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    NoRouteFoundException noRouteFoundException =
            new NoRouteFoundException(httpRequest.method().name(), httpRequest.uri());
    this.exceptionHandler.handle(requestContext, noRouteFoundException,
            HttpResponseStatus.NOT_FOUND);
  }
}
