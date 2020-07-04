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
package org.aquiver.advice;

import org.aquiver.mvc.route.RouteParam;
import org.aquiver.toolkit.ReflectionUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/7/3
 */
public class Advice {
  private Class<?> clazz;
  private Object target;
  private Class<? extends Throwable> exception;
  private String methodName;
  private List<RouteParam> params;

  public static Advice builder() {
    return new Advice();
  }

  public Object handlerException(Throwable throwable, Object[] paramValues, Class<?>[] paramTypes) throws Throwable {
    Objects.requireNonNull(throwable, "Throwable can't be null");
    Objects.requireNonNull(this.exception, "Throwable class can't be null");
    Objects.requireNonNull(this.target, "Target can't be null");
    Objects.requireNonNull(this.clazz, "Ref class can't be null");

    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    final Method method = ReflectionUtils.getInvokeMethod(clazz, methodName, paramTypes);

    return lookup.unreflect(method).bindTo(target).invokeWithArguments(paramValues);
  }

  public Object target() {
    return target;
  }

  public Advice target(Object target) {
    this.target = target;
    return this;
  }

  public Class<? extends Throwable> exception() {
    return exception;
  }

  public Advice exception(Class<? extends Throwable> exception) {
    this.exception = exception;
    return this;
  }

  public String methodName() {
    return methodName;
  }

  public Advice methodName(String methodName) {
    this.methodName = methodName;
    return this;
  }

  public List<RouteParam> params() {
    return params;
  }

  public Advice params(List<RouteParam> params) {
    this.params = params;
    return this;
  }

  public Class<?> clazz() {
    return clazz;
  }

  public Advice clazz(Class<?> clazz) {
    this.clazz = clazz;
    return this;
  }
}
