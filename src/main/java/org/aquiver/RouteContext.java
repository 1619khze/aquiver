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

import org.aquiver.annotation.JSON;
import org.aquiver.annotation.Path;
import org.aquiver.annotation.PathMethod;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author WangYi
 * @since 2020/5/23
 */
public final class RouteContext {
  private static final Logger log = LoggerFactory.getLogger(RouteContext.class);

  private final Map<String, Route> routes = new ConcurrentHashMap<>(64);
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final List<ArgsResolver> argsResolvers = new ArrayList<>();
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public List<ArgsResolver> getArgsResolvers() {
    return argsResolvers;
  }

  protected ReentrantReadWriteLock.ReadLock readLock() {
    return this.readWriteLock.readLock();
  }

  protected ReentrantReadWriteLock.WriteLock writeLock() {
    return this.readWriteLock.writeLock();
  }

  protected boolean repeat(String url) {
    return this.routes.containsKey(url);
  }

  public void loadArgsResolver() throws Exception {
    ServiceLoader<ArgsResolver> argsResolverLoad = ServiceLoader.load(ArgsResolver.class);
    for (ArgsResolver ser : argsResolverLoad) {
      ArgsResolver argsResolver = ser.getClass().getDeclaredConstructor().newInstance();
      getArgsResolvers().add(argsResolver);
    }
  }

  private void findArgsResolver(Class<?> cls) throws ReflectiveOperationException {
    Class<?>[] interfaces = cls.getInterfaces();
    if (interfaces.length == 0) {
      return;
    }
    for (Class<?> interfaceCls : interfaces) {
      if (!interfaceCls.equals(ArgsResolver.class)) {
        continue;
      }
      ArgsResolver argsResolver = (ArgsResolver) cls.getDeclaredConstructor().newInstance();
      getArgsResolvers().add(argsResolver);
    }
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

  public void addRoute(Class<?> cls, String url) throws Throwable {
    Method[] methods = cls.getMethods();
    for (Method method : methods) {
      Path methodPath = method.getAnnotation(Path.class);
      if (!Objects.isNull(methodPath)) {
        this.addRoute(cls, url, method, methodPath.value(), methodPath.method());
      }
      Annotation[] annotations = method.getAnnotations();
      if (annotations.length == 0) {
        return;
      }
      for (Annotation annotation : annotations) {
        String routeUrl = "/";
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Path path = annotationType.getAnnotation(Path.class);
        if (Objects.isNull(path)) {
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

  public void addRoute(Class<?> clazz, String baseUrl, Method method, String methodUrl, PathMethod pathMethod) {
    String completeUrl = this.getMethodUrl(baseUrl, methodUrl);
    if (completeUrl.trim().isEmpty()) {
      return;
    }

    boolean isJsonResponse = !Objects.isNull(method.getAnnotation(JSON.class));
    Route route = new Route(completeUrl, clazz, method.getName(), isJsonResponse, pathMethod);

    Parameter[] parameters = method.getParameters();
    String[] paramNames = this.getMethodParamName(method);

    this.execuArgsResolver(route, parameters, paramNames);
    if (this.repeat(completeUrl)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered request processor URL is duplicated :{}", completeUrl);
      }
      throw new RouteRepeatException("Registered request processor URL is duplicated : " + completeUrl);
    } else {
      this.addRoute(completeUrl, route);
    }
  }

  protected void addRoute(String url, Route route) {
    this.writeLock().lock();
    try {
      this.routes.put(url, route);
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  private void execuArgsResolver(Route route, Parameter[] ps, String[] paramNames) {
    for (int i = 0; i < ps.length; i++) {
      List<ArgsResolver> argsResolvers = getArgsResolvers();
      if (argsResolvers.isEmpty()) {
        break;
      }
      this.execuArgsResolver(route, ps[i], paramNames[i], argsResolvers);
    }
  }

  private void execuArgsResolver(Route route, Parameter parameter,
                                 String paramName, List<ArgsResolver> argsResolvers) {
    for (ArgsResolver argsResolver : argsResolvers) {
      if (!argsResolver.support(parameter)) continue;
      RequestHandlerParam param = argsResolver.resolve(parameter, paramName);
      if (param != null) {
        route.getParams().add(param);
      }
    }
  }

  protected String getMethodUrl(String baseUrl, String methodMappingUrl) {
    StringBuilder url = new StringBuilder(256);
    url.append((baseUrl == null || baseUrl.trim().isEmpty()) ? "" : baseUrl.trim());
    if (methodMappingUrl != null && !methodMappingUrl.trim().isEmpty()) {
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

  protected String[] getMethodParamName(final Method method) {
    Parameter[] parameters = method.getParameters();
    List<String> nameList = new ArrayList<>();
    for (Parameter param : parameters) {
      String name = param.getName();
      nameList.add(name);
    }
    return nameList.toArray(value -> new String[0]);
  }
}
