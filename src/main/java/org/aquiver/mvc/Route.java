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
package org.aquiver.mvc;

import org.aquiver.annotation.PathMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Route {
  private String url;
  private Class<?> clazz;
  private String method;
  private Object bean;
  private boolean jsonResponse;
  private PathMethod pathMethod;

  private List<RequestHandlerParam> params = new ArrayList<>();

  public Route(String url, Class<?> clazz, String method, boolean jsonResponse, PathMethod pathMethod) {
    this.url = url;
    this.clazz = clazz;
    this.method = method;
    this.jsonResponse = jsonResponse;
    this.pathMethod = pathMethod;
    try {
      this.bean = clazz.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
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

  public void setClazz(Class<?> clazz) {
    this.clazz = clazz;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public boolean isJsonResponse() {
    return jsonResponse;
  }

  public void setJsonResponse(boolean jsonResponse) {
    this.jsonResponse = jsonResponse;
  }

  public PathMethod getPathMethod() {
    return pathMethod;
  }

  public void setPathMethod(PathMethod pathMethod) {
    this.pathMethod = pathMethod;
  }

  public Object getBean() {
    return bean;
  }

  public void setBean(Object bean) {
    this.bean = bean;
  }

  public List<RequestHandlerParam> getParams() {
    return params;
  }

  public void setParams(List<RequestHandlerParam> params) {
    this.params = params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Route that = (Route) o;
    return jsonResponse == that.jsonResponse &&
            Objects.equals(url, that.url) &&
            Objects.equals(clazz, that.clazz) &&
            Objects.equals(method, that.method) &&
            Objects.equals(bean, that.bean) &&
            pathMethod == that.pathMethod &&
            Objects.equals(params, that.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, clazz, method, bean, jsonResponse, pathMethod, params);
  }

  @Override
  public String toString() {
    return "RequestHandler{" +
            "url='" + url + '\'' +
            ", clazz=" + clazz +
            ", method=" + method +
            ", bean=" + bean +
            ", jsonResponse=" + jsonResponse +
            ", requestMethod=" + pathMethod +
            ", params=" + params +
            '}';
  }
}
