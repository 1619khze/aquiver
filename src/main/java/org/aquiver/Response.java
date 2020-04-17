package org.aquiver;

import java.util.Objects;

public class Response {
  private Object  result;
  private boolean jsonResponse;

  public Response(Object result, boolean jsonResponse) {
    this.result       = result;
    this.jsonResponse = jsonResponse;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public boolean isJsonResponse() {
    return jsonResponse;
  }

  public void setJsonResponse(boolean jsonResponse) {
    this.jsonResponse = jsonResponse;
  }

  @Override
  public String toString() {
    return "Response{" +
            "result=" + result +
            ", jsonResponse=" + jsonResponse +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Response response = (Response) o;
    return jsonResponse == response.jsonResponse &&
            Objects.equals(result, response.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, jsonResponse);
  }
}
