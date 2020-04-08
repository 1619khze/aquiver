package org.aquiver.mvc;

import java.util.Objects;

public class RequestHandlerParam {
  private String    name;
  private Class<?>  dataType;
  private RequestParamType type;
  private boolean   required = true;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getDataType() {
    return dataType;
  }

  public void setDataType(Class<?> dataType) {
    this.dataType = dataType;
  }

  public RequestParamType getType() {
    return type;
  }

  public void setType(RequestParamType type) {
    this.type = type;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestHandlerParam that = (RequestHandlerParam) o;
    return required == that.required &&
            Objects.equals(name, that.name) &&
            Objects.equals(dataType, that.dataType) &&
            type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dataType, type, required);
  }

  @Override
  public String toString() {
    return "RequestHandlerParam{" +
            "name='" + name + '\'' +
            ", dataType=" + dataType +
            ", type=" + type +
            ", required=" + required +
            '}';
  }
}
