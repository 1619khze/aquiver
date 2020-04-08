package org.aquiver.mvc;

import java.util.Arrays;
import java.util.Objects;

public class LogicExecutionWrapper {
  private RequestHandler requestHandler;
  private Object[] paramValues;
  private Class<?>[] paramTypes;

  public LogicExecutionWrapper(RequestHandler requestHandler, Object[] paramValues, Class<?>[] paramTypes) {
    this.requestHandler = requestHandler;
    this.paramValues    = paramValues;
    this.paramTypes     = paramTypes;
  }

  public RequestHandler getRequestHandler() {
    return requestHandler;
  }

  public void setRequestHandler(RequestHandler requestHandler) {
    this.requestHandler = requestHandler;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LogicExecutionWrapper that = (LogicExecutionWrapper) o;
    return Objects.equals(requestHandler, that.requestHandler) &&
            Arrays.equals(paramValues, that.paramValues) &&
            Arrays.equals(paramTypes, that.paramTypes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(requestHandler);
    result = 31 * result + Arrays.hashCode(paramValues);
    result = 31 * result + Arrays.hashCode(paramTypes);
    return result;
  }

  @Override
  public String toString() {
    return "LogicExecutionWrapper{" +
            "requestHandler=" + requestHandler +
            ", paramValues=" + Arrays.toString(paramValues) +
            ", paramTypes=" + Arrays.toString(paramTypes) +
            '}';
  }
}
