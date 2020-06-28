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
package org.aquiver.route;

import org.aquiver.annotation.PathMethod;
import org.aquiver.route.views.HTMLView;
import org.aquiver.route.views.ViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Route {
  private static final Logger log = LoggerFactory.getLogger(Route.class);

  private String url;
  private Class<?> clazz;
  private String method;
  private Object bean;
  private PathMethod pathMethod;
  private Object[] paramValues;
  private Class<?>[] paramTypes;
  private Object invokeResult;
  private ViewType viewType;
  private HTMLView htmlView;

  private List<RouteParam> params = new ArrayList<>();

  private Route(String url, Class<?> clazz, String method, PathMethod pathMethod) {
    this.url = url;
    this.clazz = clazz;
    this.method = method;
    this.pathMethod = pathMethod;
    try {
      this.bean = clazz.newInstance();
    } catch (ReflectiveOperationException e) {
      log.error("An exception occurred during instantiation");
    }
  }

  public static Route of(String url, Class<?> clazz, String method, PathMethod pathMethod) {
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(clazz, "clazz must not be null");
    Objects.requireNonNull(method, "method must not be null");
    Objects.requireNonNull(pathMethod, "pathMethod must not be null");
    return new Route(url, clazz, method, pathMethod);
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

  public List<RouteParam> getParams() {
    return params;
  }

  public void setParams(List<RouteParam> params) {
    this.params = params;
  }

  public Object[] getParamValues() {
    return paramValues;
  }

  public void setParamValues(Object[] paramValues) {
    this.paramValues = paramValues;
  }

  public Class<?>[] getParamTypes() {
    return paramTypes;
  }

  public void setParamTypes(Class<?>[] paramTypes) {
    this.paramTypes = paramTypes;
  }

  public Object getInvokeResult() {
    return invokeResult;
  }

  public void setInvokeResult(Object invokeResult) {
    this.invokeResult = invokeResult;
  }

  public ViewType getViewType() {
    return viewType;
  }

  public void setViewType(ViewType viewType) {
    this.viewType = viewType;
  }

  public HTMLView getHtmlView() {
    return htmlView;
  }

  public void setHtmlView(HTMLView htmlView) {
    this.htmlView = htmlView;
  }

  @Override
  public String toString() {
    return "Route{" +
            "url='" + url + '\'' +
            ", clazz=" + clazz +
            ", method='" + method + '\'' +
            ", bean=" + bean +
            ", pathMethod=" + pathMethod +
            ", paramValues=" + Arrays.toString(paramValues) +
            ", paramTypes=" + Arrays.toString(paramTypes) +
            ", invokeResult=" + invokeResult +
            ", viewType=" + viewType +
            ", htmlView=" + htmlView +
            ", params=" + params +
            '}';
  }
}
