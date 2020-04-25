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
package org.aquiver;

import org.aquiver.mvc.MediaType;

import java.util.Objects;

public class Response {
  private Object    result;
  private boolean   jsonResponse;
  private MediaType mediaType;

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

  public MediaType getMediaType() {
    return mediaType;
  }

  public void setMediaType(MediaType mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String toString() {
    return "Response{" +
            "result=" + result +
            ", jsonResponse=" + jsonResponse +
            ", mediaType=" + mediaType +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Response response = (Response) o;
    return jsonResponse == response.jsonResponse &&
            Objects.equals(result, response.result) &&
            Objects.equals(mediaType, response.mediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, jsonResponse, mediaType);
  }
}
