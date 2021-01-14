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

import org.apex.Apex;
import org.apex.ApexContext;
import org.apex.annotation.ConfigBean;
import org.apex.annotation.PropertyBean;
import org.apex.annotation.Scheduled;
import org.apex.annotation.Singleton;
import org.aquiver.mvc.annotation.HandleAdvice;
import org.aquiver.mvc.annotation.Path;
import org.aquiver.mvc.annotation.RestPath;
import org.aquiver.mvc.annotation.RouteAdvice;
import org.aquiver.mvc.argument.AnnotationArgumentGetterResolver;
import org.aquiver.mvc.argument.ArgumentGetterResolver;
import org.aquiver.mvc.handler.RouteAdviceHandlerResolver;
import org.aquiver.mvc.handler.RouteAdviceHandlerWrapper;
import org.aquiver.mvc.router.RestfulRouter;
import org.aquiver.websocket.WebSocket;
import org.aquiver.websocket.WebSocketResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/23
 */
public class WebContextInitializer {
  private static final Logger log = LoggerFactory.getLogger(WebContextInitializer.class);

  private ApexContext context;
  private Map<String, Object> instances;

  public void initialize(Aquiver aquiver) throws Exception {
    final String scanPath = aquiver.bootCls().getPackage().getName();
    final Apex apex = aquiver.apex();

    this.context = aquiver.apexContext();
    this.instances = context.instances();

    apex.typeAnnotation(Path.class);
    apex.typeAnnotation(RouteAdvice.class);
    apex.typeAnnotation(RestPath.class);
    apex.typeAnnotation(WebSocket.class);
    apex.typeAnnotation(Singleton.class);
    apex.typeAnnotation(ConfigBean.class);
    apex.typeAnnotation(PropertyBean.class);
    apex.typeAnnotation(Scheduled.class);

    apex.packages().add(scanPath);
    apex.mainArgs(aquiver.mainArgs());

    context.init(apex);
    context.addBean(ResultHandlerResolver.class);
    context.addBean(ViewHandlerResolver.class);
    context.addBean(ArgumentGetterResolver.class);
    context.addBean(AnnotationArgumentGetterResolver.class);

    this.initRestfulRoute();
    this.initWebSocket();
    this.initRouteAdvice();
  }

  /**
   * Filter out the classes marked with Web Socket annotations
   * from the scan result set and reduce them to the Web Socket
   * list of the routing manager
   *
   * @param instances Scanned result
   */
  public void initWebSocket() {
    for (Object object : instances.values()) {
      Class<?> cls = object.getClass();
      Method[] declaredMethods = cls.getDeclaredMethods();
      if (!ReflectionHelper.isNormal(cls)
              || !cls.isAnnotationPresent(WebSocket.class)
              || declaredMethods.length == 0) continue;
      context.getBean(WebSocketResolver.class).registerWebSocket(cls);
    }
  }

  /**
   * Filter the Advice class from the scanned result
   * set and add it to the Advice Manager
   *
   * @param instances Scanned result
   */
  public void initRouteAdvice() {
    for (Object object : instances.values()) {
      Class<?> cls = object.getClass();
      Method[] declaredMethods = cls.getDeclaredMethods();
      if (!ReflectionHelper.isNormal(cls)
              || !cls.isAnnotationPresent(RouteAdvice.class)
              || declaredMethods.length == 0) {
        continue;
      }
      for (Method method : declaredMethods) {
        HandleAdvice handler = method.getDeclaredAnnotation(HandleAdvice.class);
        if (Objects.isNull(handler)) {
          continue;
        }
        RouteAdviceHandlerWrapper errorHandlerWrapper = new RouteAdviceHandlerWrapper();
        errorHandlerWrapper.initialize(cls);
        context.getBean(RouteAdviceHandlerResolver.class)
                .registerAdviceHandler(handler.value(), errorHandlerWrapper);
      }
    }
  }

  /**
   * Filter out the routing class from the scanned
   * result set and add it to the routing manager
   *
   * @param instances Scanned result
   * @param aquiver   aquiver
   */
  public void initRestfulRoute() {
    for (Map.Entry<String, Object> entry : instances.entrySet()) {
      Class<?> next = entry.getValue().getClass();
      String url = "/";
      boolean normal = ReflectionHelper.isNormal(next);
      if (!normal) {
        continue;
      }
      if (log.isDebugEnabled()) {
        log.debug("Is this class normal: {}", next.getSimpleName());
      }
      if (!next.isAnnotationPresent(Path.class) &&
              !next.isAnnotationPresent(RestPath.class)) {
        continue;
      }
      if (next.isAnnotationPresent(Path.class) &&
              next.isAnnotationPresent(RestPath.class)) {
        throw new IllegalArgumentException("There are duplicate annotations in "
                + next.getName() + ": @" + Path.class.getSimpleName() + " @" + RestPath.class.getSimpleName());
      }
      url = url(next, url);
      if (!url.startsWith("/")) {
        url = "/" + url;
      }
      context.getBean(RestfulRouter.class).registerRoute(url, entry.getValue());
    }
  }

  /**
   * Get path value
   *
   * @param cls route class
   * @param url default url
   * @return The complete url after stitching
   */
  private String url(Class<?> cls, String url) {
    String pathValue = "/";
    if (cls.isAnnotationPresent(Path.class)) {
      Path path = cls.getAnnotation(Path.class);
      if (path.value().equals("")) {
        return url;
      }
      pathValue = path.value();
    }
    if (cls.isAnnotationPresent(RestPath.class)) {
      RestPath restPath = cls.getAnnotation(RestPath.class);
      if (restPath.value().equals("")) {
        return url;
      }
      pathValue = restPath.value();
    }
    String className = cls.getName();
    if (!"".equals(pathValue)) {
      url = pathValue;
    }
    if (log.isDebugEnabled()) {
      log.debug("Registered rest controller:[{}:{}]", url, className);
    }
    return url;
  }
}
