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

import org.aquiver.mvc.annotation.HttpMethod;

import java.lang.reflect.Method;
import java.util.Objects;

public class RouteInfo {
  private String url;
  private Class<?> clazz;
  private Method method;
  private Object bean;
  private HttpMethod httpMethod;

  private RouteInfo(String url, Class<?> clazz, Object bean, Method method, HttpMethod httpMethod) {
    this.url = url;
    this.clazz = clazz;
    this.method = method;
    this.httpMethod = httpMethod;
    this.bean = bean;
  }

  public static RouteInfo of(String url, Class<?> clazz, Object bean, Method method, HttpMethod httpMethod) {
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(clazz, "clazz must not be null");
    Objects.requireNonNull(method, "method must not be null");
    Objects.requireNonNull(httpMethod, "pathMethod must not be null");
    return new RouteInfo(url, clazz, bean, method, httpMethod);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Object getBean() {
    return bean;
  }

  @Override
  public String toString() {
    return "Route{" +
            "url='" + url + '\'' +
            ", clazz=" + clazz +
            ", method='" + method + '\'' +
            ", bean=" + bean +
            ", pathMethod=" + httpMethod +
            '}';
  }
}
