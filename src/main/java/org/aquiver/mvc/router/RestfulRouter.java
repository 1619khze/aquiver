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
package org.aquiver.mvc.router;

import org.aquiver.RequestContext;
import org.aquiver.RequestHandler;
import org.aquiver.RouteRepeatException;
import org.aquiver.mvc.annotation.HttpMethod;
import org.aquiver.mvc.annotation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/5/23
 */
public class RestfulRouter implements Router {
  private static final Logger log = LoggerFactory.getLogger(RestfulRouter.class);

  private final Map<String, RouteInfo> routes = new ConcurrentHashMap<>(64);
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  @Override
  public void registerRoute(String path, Object object) {
    try {
      Class<?> cls = object.getClass();
      Method[] methods = cls.getDeclaredMethods();
      for (Method method : methods) {
        Path methodPath = method.getAnnotation(Path.class);
        if (Objects.nonNull(methodPath)) {
          String completeUrl = this.getMethodUrl(path, methodPath.value());
          this.registerRoute(cls, object, completeUrl, method, methodPath.method());
        }
        registerRoute(cls, object, path, method);
      }
    } catch (Throwable throwable) {
      log.error("Register route exception", throwable);
    }
  }

  @Override
  public RouteInfo lookup(String url) {
    String lookupPath = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }

    if (lookupPath.equals("")) {
      lookupPath = "/";
    }

    RouteInfo routeInfo = routes.get(url);
    if (Objects.nonNull(routeInfo)) {
      return routeInfo;
    }

    for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
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
    return routeInfo;
  }

  @Override
  public void registerRoute(String path, RequestHandler handler, HttpMethod httpMethod) {
    try {
      Class<? extends RequestHandler> ref = handler.getClass();
      Method handle = ref.getMethod("handle", RequestContext.class);
      this.registerRoute(RequestHandler.class, handler, path, handle, httpMethod);
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
  private void registerRoute(Class<?> cls, Object bean, String url, Method method) throws Throwable {
    Annotation[] annotations = method.getAnnotations();
    if (annotations.length != 0) {
      for (Annotation annotation : annotations) {
        String routeUrl = "/";
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Path path = annotationType.getAnnotation(Path.class);
        if (Objects.isNull(path)) {
          continue;
        }
        HttpMethod httpMethod = path.method();
        Method valueMethod = annotationType.getMethod("value");
        Object valueInvokeResult = lookup.unreflect(valueMethod).bindTo(annotation).invoke();
        if (!Objects.isNull(valueInvokeResult) && !valueInvokeResult.equals(routeUrl)) {
          if (valueInvokeResult.equals("")) {
            valueInvokeResult = method.getName();
          }
          routeUrl = String.join(routeUrl, String.valueOf(valueInvokeResult));
        }
        String completeUrl = this.getMethodUrl(url, routeUrl);
        this.registerRoute(cls, bean, completeUrl, method, httpMethod);
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
  private void registerRoute(Class<?> clazz, Object bean, String completeUrl, Method method, HttpMethod httpMethod) {
    if (completeUrl.trim().isEmpty()) {
      return;
    }
    RouteInfo routeInfo = createRoute(clazz, bean, method, httpMethod, completeUrl);
    if (this.routes.containsKey(completeUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered request route URL is duplicated :{}", completeUrl);
      }
      throw new RouteRepeatException("Registered request route URL is duplicated : " + completeUrl);
    } else {
      this.registerRoute(completeUrl, routeInfo);
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
  private RouteInfo createRoute(Class<?> clazz, Object bean, Method method, HttpMethod httpMethod, String completeUrl) {
    return RouteInfo.create(completeUrl, clazz, bean, method, httpMethod);
  }

  /**
   * add route
   *
   * @param url       url
   * @param routeInfo Route info
   */
  private void registerRoute(String url, RouteInfo routeInfo) {
    this.routes.put(url, routeInfo);
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
