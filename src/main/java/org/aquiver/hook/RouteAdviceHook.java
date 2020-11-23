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
package org.aquiver.hook;

import org.apex.ApexContext;
import org.aquiver.Aquiver;
import org.aquiver.ReflectionHelper;
import org.aquiver.mvc.annotation.HandleAdvice;
import org.aquiver.mvc.annotation.RouteAdvice;
import org.aquiver.mvc.handler.RouteAdviceHandlerResolver;
import org.aquiver.mvc.handler.RouteAdviceHandlerWrapper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/12
 */
public class RouteAdviceHook implements InitializeHook {
  /**
   * Filter the Advice class from the scanned result
   * set and add it to the Advice Manager
   *
   * @param instances Scanned result
   */
  @Override
  public void load(Map<String, Object> instances, Aquiver aquiver) {
    final ApexContext context = aquiver.apexContext();
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
}
