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
package org.aquiver.mvc.route;

import org.aquiver.RequestContext;
import org.aquiver.RequestHandler;
import org.aquiver.mvc.annotation.*;
import org.aquiver.mvc.resolver.ParamResolverManager;
import org.aquiver.mvc.route.views.PebbleHTMLView;
import org.aquiver.mvc.route.views.ViewType;
import org.aquiver.utils.ReflectionUtils;
import org.aquiver.websocket.WebSocket;
import org.aquiver.websocket.WebSocketChannel;
import org.aquiver.websocket.WebSocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author WangYi
 * @since 2020/5/23
 */
public final class RouteManager {
  private static final Logger log = LoggerFactory.getLogger(RouteManager.class);

  private final Map<String, Route> routes = new ConcurrentHashMap<>(64);
  private final Map<String, WebSocketChannel> webSockets = new ConcurrentHashMap<>(4);
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private ParamResolverManager resolverManager;

  public void setResolverManager(ParamResolverManager resolverManager) {
    this.resolverManager = resolverManager;
  }

  protected ReentrantReadWriteLock.ReadLock readLock() {
    return this.readWriteLock.readLock();
  }

  protected ReentrantReadWriteLock.WriteLock writeLock() {
    return this.readWriteLock.writeLock();
  }

  protected boolean whetherRouteIsDuplicated(String url) {
    return this.routes.containsKey(url);
  }

  public void removeRoute(String url) {
    this.writeLock().lock();
    try {
      this.routes.remove(url);
    } finally {
      this.writeLock().unlock();
    }
  }

  /**
   * Get WebSocket Route Map
   *
   * @return WebSocket Route Map
   */
  public Map<String, WebSocketChannel> getWebSockets() {
    return webSockets;
  }

  /**
   * Get Route Map
   *
   * @return Route Map
   */
  public Map<String, Route> getRoutes() {
    readLock().lock();
    try {
      return this.routes;
    } finally {
      readLock().unlock();
    }
  }

  /**
   * add route
   *
   * @param object route object
   * @param url @Path value
   */
  public void addRoute(Object object, String url) {
    try {
      Class<?> cls = object.getClass();
      Method[] methods = cls.getMethods();
      for (Method method : methods) {
        Path methodPath = method.getAnnotation(Path.class);
        if (Objects.nonNull(methodPath)) {
          String completeUrl = this.getMethodUrl(url, methodPath.value());
          this.addRoute(cls, object, completeUrl, method, methodPath.method());
        }
        addRoute(cls, object, url, method);
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public void addRoute(String path, RequestHandler handler, HttpMethod httpMethod) {
    try {
      Class<? extends RequestHandler> ref = handler.getClass();
      Method handle = ref.getMethod("handle", RequestContext.class);
      this.addRoute(RequestHandler.class, handler, path, handle, httpMethod);
    } catch (NoSuchMethodException e) {
      log.error("There is no such method {}", "handle", e);
    }
  }

  /**
   * add route
   *
   * @param cls    route class
   * @param url    @GET/@POST.. value
   * @param method Mapping annotation annotation method
   * @throws Throwable reflection exception
   */
  private void addRoute(Class<?> cls, Object bean, String url, Method method) throws Throwable {
    Annotation[] annotations = method.getAnnotations();
    if (annotations.length != 0) {
      for (Annotation annotation : annotations) {
        String routeUrl = "/";
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Path path = annotationType.getAnnotation(Path.class);
        RestPath restPath = annotationType.getAnnotation(RestPath.class);
        if (Objects.isNull(path) && Objects.isNull(restPath)) {
          continue;
        }
        HttpMethod httpMethod = path.method();
        Method valueMethod = annotationType.getMethod("value");
        Object valueInvokeResult = lookup.unreflect(valueMethod).bindTo(annotation).invoke();
        if (!Objects.isNull(valueInvokeResult) && !valueInvokeResult.equals(routeUrl)) {
          routeUrl = String.join(routeUrl, String.valueOf(valueInvokeResult));
        }
        String completeUrl = this.getMethodUrl(url, routeUrl);
        this.addRoute(cls, bean, completeUrl, method, httpMethod);
      }
    }
  }

  /**
   * add route
   *
   * @param clazz      route class
   * @param method     Mapping annotation annotation method
   * @param httpMethod http method
   */
  public void addRoute(Class<?> clazz, Object bean, String completeUrl, Method method, HttpMethod httpMethod) {
    if (completeUrl.trim().isEmpty()) {
      return;
    }
    Route route = createRoute(clazz, bean, method, httpMethod, completeUrl);

    Parameter[] parameters = method.getParameters();
    String[] paramNames = ReflectionUtils.getMethodParamName(method);

    List<RouteParam> routeParams = resolverManager.invokeParamResolver(parameters, paramNames);
    route.setParams(routeParams);

    if (this.whetherRouteIsDuplicated(completeUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered request route URL is duplicated :{}", completeUrl);
      }
      throw new RouteRepeatException("Registered request route URL is duplicated : " + completeUrl);
    } else {
      this.addRoute(completeUrl, route);
    }
  }

  /**
   * Create route
   *
   * @param clazz       Route class
   * @param method      Route method
   * @param httpMethod  Route root path
   * @param completeUrl Complete route path
   * @return Route
   */
  private Route createRoute(Class<?> clazz, Object bean, Method method, HttpMethod httpMethod, String completeUrl) {
    Route route = Route.of(completeUrl, clazz, bean, method.getName(), httpMethod);

    boolean isAllJsonResponse = Objects.isNull(clazz.getAnnotation(RestPath.class));
    boolean isJsonResponse = Objects.isNull(method.getAnnotation(JSON.class));
    boolean isViewResponse = Objects.isNull(method.getAnnotation(View.class));

    if (isJsonResponse || (isAllJsonResponse && isViewResponse)) {
      route.setViewType(ViewType.JSON);
    }
    if (!isViewResponse) {
      route.setViewType(ViewType.HTML);
      route.setHtmlView(new PebbleHTMLView());
    }
    if (isAllJsonResponse && isJsonResponse && isViewResponse) {
      route.setViewType(ViewType.TEXT);
    }
    return route;
  }

  /**
   * add route
   *
   * @param url   url
   * @param route Route info
   */
  protected void addRoute(String url, Route route) {
    this.writeLock().lock();
    try {
      this.routes.put(url, route);
    } finally {
      this.writeLock().unlock();
    }
  }

  /**
   * add websocket route
   *
   * @param webSocketClass Classes annotated with @WebSocket annotation
   *                       and implementing the WebSocketChannel interface
   * @throws ReflectiveOperationException Exception superclass when
   *                                      performing reflection operation
   */
  public void addWebSocket(Class<?> webSocketClass) throws ReflectiveOperationException {
    WebSocket webSocket = webSocketClass.getAnnotation(WebSocket.class);
    if (Objects.isNull(webSocket)) {
      return;
    }
    String webSocketPath = webSocket.value();
    if (webSocketPath.equals("")) {
      webSocketPath = "/";
    }

    for (Class<?> interfaceCls : webSocketClass.getInterfaces()) {
      if (!interfaceCls.isAssignableFrom(WebSocketChannel.class)) {
        continue;
      }
      final WebSocketChannel webSocketChannel =
              (WebSocketChannel) webSocketClass.newInstance();
      this.addWebSocket(webSocketPath, webSocketChannel);
    }
    this.addWebSocket(webSocketPath, webSocketClass);
  }

  private void addWebSocket(String webSocketPath, Class<?> webSocketClass) {
    List<Class<?>> classes = Arrays.asList(webSocketClass.getInterfaces());
    if (classes.contains(WebSocketChannel.class)) {
      return;
    }
    final WebSocketWrapper wrapper = new WebSocketWrapper(resolverManager);
    wrapper.initialize(webSocketClass);
    this.addWebSocket(webSocketPath, wrapper);
  }

  /**
   * add websocket route
   *
   * @param webSocketPath    WebSocket url path
   * @param webSocketChannel WebSocket abstract interface
   */
  public void addWebSocket(String webSocketPath, WebSocketChannel webSocketChannel) {
    if (this.whetherRouteIsDuplicated(webSocketPath)
            || this.webSockets.containsKey(webSocketPath)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered websocket channel URL is duplicated :{}", webSocketPath);
      }
      throw new RouteRepeatException("Registered websocket channel URL is duplicated : " + webSocketPath);
    }
    this.webSockets.put(webSocketPath, webSocketChannel);
  }

  /**
   * Get the complete mapped address
   *
   * @param baseUrl          The address of @Path or @RestPath on the class
   * @param methodMappingUrl Annotated address on method
   * @return complete mapped address
   */
  protected String getMethodUrl(String baseUrl, String methodMappingUrl) {
    StringBuilder url = new StringBuilder(256);
    url.append((baseUrl == null || baseUrl.trim().isEmpty()) ? "" : baseUrl.trim());
    if (Objects.nonNull(methodMappingUrl) && !methodMappingUrl.trim().isEmpty()) {
      String methodMappingUrlTrim = methodMappingUrl.trim();
      if (!methodMappingUrlTrim.startsWith("/")) {
        methodMappingUrlTrim = "/" + methodMappingUrlTrim;
      }
      if (url.toString().endsWith("/")) {
        url.setLength(url.length() - 1);
      }
      url.append(methodMappingUrlTrim);
    }
    return url.toString();
  }
}
