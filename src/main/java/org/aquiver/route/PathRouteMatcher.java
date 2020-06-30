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
package org.aquiver.route;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.aquiver.Async;
import org.aquiver.RequestContext;
import org.aquiver.handler.HttpExceptionHandler;
import org.aquiver.server.StaticFileServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author WangYi
 * @since 2020/5/29
 */
public class PathRouteMatcher implements RouteMatcher<RequestContext> {
  private static final Logger log = LoggerFactory.getLogger(PathRouteMatcher.class);

  private final Map<String, Route> routeMap;
  private final HttpExceptionHandler exceptionHandler;
  private final StaticFileServerHandler fileServerHandler;
  private final RouteManager routeManager;

  public PathRouteMatcher(RouteManager manager) {
    this.routeManager = manager;
    this.routeMap = manager.getRoutes();
    this.exceptionHandler = new HttpExceptionHandler();
    this.fileServerHandler = new StaticFileServerHandler();
  }

  @Override
  public RequestContext match(RequestContext context) {
    CompletableFuture<RequestContext> lookUpaFuture =
            CompletableFuture.completedFuture(context);

    CompletableFuture<Route> resultFuture = lookUpaFuture
            .thenApply(this::lookupRoute)
            .thenApply(this::invokeParam)
            .thenApply(this::invokeMethod);

    Route route = Async.getIfReady(resultFuture);
    context.route(route);
    resultFuture.complete(route);
    return context;
  }

  private Route invokeParam(RequestContext context) {
    Route route = context.route();
    List<RouteParam> params = route.getParams();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];

    log.info("{} {} {}", context.request().ipAddress(),
            context.request().httpMethodName(), context.route().getUrl());

    try {
      for (int i = 0; i < paramValues.length; i++) {
        RouteParam handlerParam = route.getParams().get(i);
        paramTypes[i] = handlerParam.getDataType();
        paramValues[i] = this.routeManager.dispen(
                handlerParam, context, route.getUrl()
        );
      }
    } catch (Exception e) {
      log.error("An exception occurred when obtaining invoke param");
    }
    route.setParamValues(paramValues);
    route.setParamTypes(paramTypes);
    return route;
  }

  private Route invokeMethod(Route route) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      Method method = this.getInvokeMethod(route);
      Object result = lookup.unreflect(method).bindTo(route.getBean())
              .invokeWithArguments(route.getParamValues());

      route.setInvokeResult(result);
      return route;
    } catch (Throwable throwable) {
      log.error("An exception occurred when calling the mapping method", throwable);
    }
    return null;
  }

  private Method getInvokeMethod(Route route) throws NoSuchMethodException {
    return route.getClazz().getMethod(route.getMethod(), route.getParamTypes());
  }

  private Route loopLookUp(String lookupPath) {
    if (routeMap.containsKey(lookupPath)) {
      return this.routeMap.get(lookupPath);
    }
    for (Map.Entry<String, Route> entry : routeMap.entrySet()) {
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      String matcher = PathVarMatcher.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher) || lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }
      if (PathVarMatcher.checkMatch(lookupPathSplit, mappingUrlSplit)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private RequestContext lookupRoute(RequestContext context) {
    String lookupPath = lookupPath(context.request().uri());
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }
    Route route = loopLookUp(lookupPath);
    if (!Objects.isNull(route)) {
      context.route(route);
      return context;
    } else {
      return lookupStaticFile(context);
    }
  }

  private String lookupPath(String uri) {
    return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
  }

  private RequestContext lookupStaticFile(RequestContext context) {
    try {
      final boolean result = this.fileServerHandler.handle(context);
      if (!result) {
        this.handlerNoRouteFoundException(context);
        throw new NoRouteFoundException(context.request().httpMethodName(), context.request().uri());
      }
      return context;
    } catch (Exception e) {
      log.error("An exception occurred while looking up the route", e);
      return context;
    }
  }

  private void handlerNoRouteFoundException(RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.request().httpRequest();
    NoRouteFoundException noRouteFoundException =
            new NoRouteFoundException(httpRequest.method().name(), httpRequest.uri());
    this.exceptionHandler.handle(requestContext, noRouteFoundException,
            HttpResponseStatus.NOT_FOUND);
  }
}
