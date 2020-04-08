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
