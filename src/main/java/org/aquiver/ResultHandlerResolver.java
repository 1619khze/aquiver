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

import org.apex.ApexContext;
import org.aquiver.result.StringResultHandler;
import org.aquiver.result.VoidResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public final class ResultHandlerResolver {
  private static final Logger log = LoggerFactory.getLogger(ResultHandlerResolver.class);
  private final Map<Class<?>, ResultHandler<?>> mapping = new IdentityHashMap<>();
  private final ApexContext context = ApexContext.of();

  public ResultHandlerResolver() {
    this.init();
  }

  private void init() {
    this.register(Void.TYPE, VoidResultHandler.class);
    this.register(String.class, StringResultHandler.class);
  }

  public void register(Class<?> resultClass, Class<? extends ResultHandler<?>> resultHandlerClass) {
    Objects.requireNonNull(resultClass, "resultClass can't be null");
    Objects.requireNonNull(resultHandlerClass, "resultHandlerClass can't be null");

    if (log.isDebugEnabled()) {
      log.debug("register ResultHandler: {} -> {}", resultClass.getName(), resultHandlerClass.getName());
    }
    ResultHandler<?> resultHandler = this.context.addBean(resultHandlerClass);
    this.mapping.put(resultClass, resultHandler);
  }

  @SuppressWarnings("unchecked")
  public ResultHandler<Object> lookup(Class<?> resultClass) {
    ResultHandler<Object> resultHandler = (ResultHandler<Object>) mapping.get(resultClass);
    if (resultHandler == null) {
      // Special code for Object.class as result
      for (Map.Entry<Class<?>, ResultHandler<?>> entry : mapping.entrySet()) {
        Class<?> targetClass = entry.getKey();
        if ((targetClass != Object.class) && targetClass.isAssignableFrom(resultClass)) {
          return (ResultHandler<Object>) entry.getValue();
        }
      }
      throw new IllegalStateException("Unsupported result class: " + resultClass.getName());
    }
    return resultHandler;
  }
}
