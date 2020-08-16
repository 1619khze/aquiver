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
package org.aquiver.loader;

import org.aquiver.Aquiver;
import org.aquiver.function.Advice;
import org.aquiver.mvc.annotation.advice.ExceptionHandler;
import org.aquiver.mvc.annotation.advice.RouteAdvice;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/12
 */
public class ExceptionAdviceLoader implements WebLoader {
  /**
   * Filter the Advice class from the scanned result
   * set and add it to the Advice Manager
   *
   * @param instances Scanned result
   */
  @Override
  public void load(Map<String, Object> instances, Aquiver aquiver) {
    for (Object object : instances.values()) {
      Class<?> cls = object.getClass();
      Method[] declaredMethods = cls.getDeclaredMethods();
      if (!ReflectionUtils.isNormal(cls)
              || !cls.isAnnotationPresent(RouteAdvice.class)
              || declaredMethods.length == 0) {
        continue;
      }
      for (Method method : declaredMethods) {
        ExceptionHandler declaredAnnotation =
                method.getDeclaredAnnotation(ExceptionHandler.class);
        if (Objects.isNull(declaredAnnotation)) {
          continue;
        }
        Parameter[] parameters = method.getParameters();
        String[] paramNames = ReflectionUtils.getMethodParamName(method);
        List<RouteParam> routeParams = aquiver.resolverManager().invokeParamResolver(parameters, paramNames);

        Advice advice = Advice.builder().clazz(cls).exception(declaredAnnotation.value())
                .methodName(method.getName()).params(routeParams).target(object);
        aquiver.adviceManager().addAdvice(declaredAnnotation.value(), advice);
      }
    }
  }
}
