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

import org.aquiver.Aquiver;
import org.aquiver.annotation.*;
import org.aquiver.mvc.resolver.ParamResolverManager;
import org.aquiver.mvc.route.views.PebbleHTMLView;
import org.aquiver.mvc.route.views.ViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  protected ReentrantReadWriteLock.ReadLock readLock() {
    return this.readWriteLock.readLock();
  }

  protected ReentrantReadWriteLock.WriteLock writeLock() {
    return this.readWriteLock.writeLock();
  }

  protected boolean repeat(String url) {
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
   * @param cls route class
   * @param url @Path value
   * @throws Throwable reflection exception
   */
  public void addRoute(Class<?> cls, String url) throws Throwable {
    Method[] methods = cls.getMethods();
    for (Method method : methods) {
      Path methodPath = method.getAnnotation(Path.class);
      if (!Objects.isNull(methodPath)) {
        this.addRoute(cls, url, method, methodPath.value(), methodPath.method());
      }
      addRoute(cls, url, method);
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
  private void addRoute(Class<?> cls, String url, Method method) throws Throwable {
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
        PathMethod pathMethod = path.method();
        Method valueMethod = annotationType.getMethod("value");
        Object valueInvokeResult = lookup.unreflect(valueMethod).bindTo(annotation).invoke();
        if (!Objects.isNull(valueInvokeResult) && !valueInvokeResult.equals(routeUrl)) {
          routeUrl = String.join(routeUrl, String.valueOf(valueInvokeResult));
        }
        this.addRoute(cls, url, method, routeUrl, pathMethod);
      }
    }
  }

  /**
   * add route
   *
   * @param clazz      route class
   * @param baseUrl    url
   * @param method     Mapping annotation annotation method
   * @param methodUrl  method url
   * @param pathMethod http method
   */
  public void addRoute(Class<?> clazz, String baseUrl, Method method, String methodUrl, PathMethod pathMethod) {
    String completeUrl = this.getMethodUrl(baseUrl, methodUrl);
    if (completeUrl.trim().isEmpty()) {
      return;
    }
    Route route = builderRoute(clazz, method, pathMethod, completeUrl);

    ParamResolverManager resolverManager = Aquiver.of().resolverManager();
    Parameter[] parameters = method.getParameters();
    String[] paramNames = resolverManager.getMethodParamName(method);

    List<RouteParam> routeParams = resolverManager.invokeParamResolver(parameters, paramNames);
    route.setParams(routeParams);

    if (this.repeat(completeUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered request processor URL is duplicated :{}", completeUrl);
      }
      throw new RouteRepeatException("Registered request processor URL is duplicated : " + completeUrl);
    } else {
      this.addRoute(completeUrl, route);
    }
  }

  private Route builderRoute(Class<?> clazz, Method method, PathMethod pathMethod, String completeUrl) {
    Route route = Route.of(completeUrl, clazz, method.getName(), pathMethod);

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
      this.readWriteLock.writeLock().unlock();
    }
  }

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
