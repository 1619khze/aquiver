package org.aquiver.mvc;

import org.aquiver.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestHandler {
  private String        url;
  private Class<?>      clazz;
  private String        method;
  private Object        bean;
  private boolean       jsonResponse;
  private RequestMethod requestMethod;

  private List<RequestHandlerParam> params = new ArrayList<>();

  public RequestHandler(String url, Class<?> clazz, String method, boolean jsonResponse, RequestMethod requestMethod) {
    this.url           = url;
    this.clazz         = clazz;
    this.method        = method;
    this.jsonResponse  = jsonResponse;
    this.requestMethod = requestMethod;
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

  public RequestMethod getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(RequestMethod requestMethod) {
    this.requestMethod = requestMethod;
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
    RequestHandler that = (RequestHandler) o;
    return jsonResponse == that.jsonResponse &&
            Objects.equals(url, that.url) &&
            Objects.equals(clazz, that.clazz) &&
            Objects.equals(method, that.method) &&
            Objects.equals(bean, that.bean) &&
            requestMethod == that.requestMethod &&
            Objects.equals(params, that.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, clazz, method, bean, jsonResponse, requestMethod, params);
  }

  @Override
  public String toString() {
    return "RequestHandler{" +
            "url='" + url + '\'' +
            ", clazz=" + clazz +
            ", method=" + method +
            ", bean=" + bean +
            ", jsonResponse=" + jsonResponse +
            ", requestMethod=" + requestMethod +
            ", params=" + params +
            '}';
  }
}
