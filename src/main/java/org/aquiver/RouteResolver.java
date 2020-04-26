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

import org.aquiver.annotation.Controller;
import org.aquiver.annotation.RequestMapping;
import org.aquiver.annotation.RestController;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestMappingRegistry;
import org.aquiver.toolkit.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

public class RouteResolver {

  private static final Logger log = LoggerFactory.getLogger(RouteResolver.class);

  private final Discoverer discoverer;
  private final String     scanPath;

  private final RequestMappingRegistry mappingRegistry;

  public RouteResolver(Discoverer discoverer, String scanPath) {
    this.discoverer      = discoverer;
    this.scanPath        = scanPath;
    this.mappingRegistry = new RequestMappingRegistry();
  }

  public void start() throws ReflectiveOperationException {
    final Set<Class<?>> discover = discoverer.discover(scanPath);
    for (Class<?> next : discover) {
      boolean normal = Reflections.isNormal(next);
      if (!normal) {
        discover.remove(next);
      }
      if (log.isDebugEnabled()) {
        log.debug("Is this class normal: {}", normal);
      }
    }
    this.registerHandler(discover);
  }

  private void registerHandler(Set<Class<?>> discover) throws ReflectiveOperationException {
    ServiceLoader<ArgsResolver> argsResolverLoad = ServiceLoader.load(ArgsResolver.class);
    for (ArgsResolver ser : argsResolverLoad) {
      ArgsResolver argsResolver = ser.getClass().getDeclaredConstructor().newInstance();
      mappingRegistry.getArgsResolvers().add(argsResolver);
    }
    for (Class<?> cls : discover) {
      String url = "/";
      this.findArgsResolver(cls);
      if (Objects.isNull(cls.getAnnotation(RestController.class)) &&
              Objects.isNull(cls.getAnnotation(Controller.class))) {
        continue;
      }
      url = getRequestMappingPath(cls, url);
      this.registerHandlerMethod(cls, url);
    }
  }

  private void registerHandlerMethod(Class<?> cls, String url) {
    Method[] methods = cls.getMethods();
    for (Method method : methods) {
      RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
      if (Objects.isNull(methodRequestMapping)) {
        continue;
      }
      this.mappingRegistry.register(cls, url, method, methodRequestMapping);
    }
  }

  private String getRequestMappingPath(Class<?> cls, String url) {
    RequestMapping classRequestMapping = cls.getAnnotation(RequestMapping.class);
    if (classRequestMapping != null) {
      String className = cls.getName();
      String value     = classRequestMapping.value();
      if (!"".equals(value)) {
        url = value;
      }
      if (log.isDebugEnabled()) {
        log.debug("Registered rest controller:[{}:{}]", url, className);
      }
    }
    return url;
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
      mappingRegistry.getArgsResolvers().add(argsResolver);
    }
  }

  public RequestMappingRegistry getMappingRegistry() {
    return mappingRegistry;
  }
}

