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
