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
package org.aquiver.websocket;

import org.apex.ApexContext;
import org.aquiver.mvc.argument.MethodArgumentGetter;
import org.aquiver.websocket.action.OnClose;
import org.aquiver.websocket.action.OnConnect;
import org.aquiver.websocket.action.OnError;
import org.aquiver.websocket.action.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author WangYi
 * @since 2020/7/13
 */
public class WebSocketWrapper implements WebSocketChannel {
  private static final Logger log = LoggerFactory.getLogger(WebSocketWrapper.class);

  private final ApexContext apexContext = ApexContext.instance();
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final Map<Class<? extends Annotation>, Method> methodCache;
  private MethodArgumentGetter methodArgumentGetter;
  private Class<?> webSocketClass;

  public WebSocketWrapper() {
    this.methodCache = new HashMap<>();
  }

  public void initialize(Class<?> webSocketClass) {
    this.webSocketClass = webSocketClass;
    Method[] methods = webSocketClass.getMethods();
    if (methods.length == 0) {
      return;
    }
    final Map<Class<? extends Annotation>, Method>
            cache = new HashMap<>(4);

    cacheMethod(cache, methods, OnClose.class);
    cacheMethod(cache, methods, OnConnect.class);
    cacheMethod(cache, methods, OnError.class);
    cacheMethod(cache, methods, OnMessage.class);

    if (cache.isEmpty()) {
      throw new RuntimeException("Do not found any annotation of " +
              "[@OnClose / @OnConnect / @OnError /@OnMessage] in class: " + webSocketClass.getName());
    }
    this.methodCache.putAll(cache);
  }

  private void cacheMethod(Map<Class<? extends Annotation>, Method> cache, Method[] methods,
                           Class<? extends Annotation> filter) {
    List<Method> methodList = Stream.of(methods)
            .filter(method -> method.isAnnotationPresent(filter))
            .collect(Collectors.toList());

    if (methodList.size() == 1) {
      cache.put(filter, methodList.get(0));
    } else if (methodList.size() > 1) {
      throw new RuntimeException("Duplicate annotation @" + filter.getSimpleName()
              + " in class: " + methodList.get(0).getDeclaringClass().getName());
    }
  }

  @Override
  public void onConnect(WebSocketContext webSocketContext) {
    invokeAction(OnConnect.class, webSocketContext);
  }

  @Override
  public void onMessage(WebSocketContext message) {
    invokeAction(OnMessage.class, message);
  }

  @Override
  public void onClose(WebSocketContext webSocketContext) {
    invokeAction(OnClose.class, webSocketContext);
  }

  @Override
  public void onError(WebSocketContext webSocketContext) {
    invokeAction(OnError.class, webSocketContext);
  }

  private void invokeAction(Class<? extends Annotation> actionAnnotation, WebSocketContext webSocketContext) {
    if (Objects.isNull(methodArgumentGetter)) {
      this.methodArgumentGetter = apexContext.getBean(MethodArgumentGetter.class);
    }

    if (Objects.isNull(methodArgumentGetter)) {
      throw new IllegalArgumentException("methodArgumentGetter can't be null");
    }
    if (!methodCache.containsKey(actionAnnotation)) {
      return;
    }
    Method method = methodCache.get(actionAnnotation);
    try {
      final List<Object> invokeArguments = this.methodArgumentGetter.getParams(method.getParameters());
      this.lookup.unreflect(method).bindTo(webSocketClass.newInstance())
              .invokeWithArguments(invokeArguments);
    } catch (Throwable e) {
      log.error("An exception occurred when obtaining invoke param", e);
      webSocketContext.disconnect();
    }
  }
}
