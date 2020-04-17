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

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestContext {
  private final FullHttpRequest  request;
  private       FullHttpResponse response;

  private Map<String, Object> cookies;
  private Map<String, Object> headers;
  private Map<String, String> queryString;
  private Map<String, Object> formData;
  private Map<String, Object> jsonData;

  private String uri;
  private String httpMethod;
  private String version;

  public RequestContext(FullHttpRequest request) {
    this.request     = request;
    this.cookies     = new HashMap<>();
    this.headers     = new HashMap<>();
    this.queryString = new HashMap<>();
    this.formData    = new HashMap<>();
    this.jsonData    = new HashMap<>();
    this.init();
  }

  public void init() {
    this.uri        = request.uri();
    this.httpMethod = request.method().name();
    this.version    = request.protocolVersion().text();
    this.headers();
    this.cookies();
    this.queryString();
    this.formData();
    this.jsonData();
  }

  private void jsonData() {
    ByteBuf content    = request.content();
    byte[]  reqContent = new byte[content.readableBytes()];
    content.readBytes(reqContent);
    String     strContent = new String(reqContent, StandardCharsets.UTF_8);
    JSONObject jsonParams = JSONObject.parseObject(strContent);
    if (Objects.isNull(jsonParams) || jsonParams.keySet().isEmpty()) {
      return;
    }
    for (Object key : jsonParams.keySet()) {
      jsonData.put(key.toString(), jsonParams.get(key.toString()));
    }
  }

  private void formData() {
    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
            new DefaultHttpDataFactory(false), request);
    List<InterfaceHttpData> formDataList = decoder.getBodyHttpDatas();
    if (Objects.isNull(formDataList) || formDataList.isEmpty()) {
      return;
    }
    for (InterfaceHttpData data : formDataList) {
      if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
        MemoryAttribute attribute = (MemoryAttribute) data;
        formData.put(attribute.getName(), attribute.getValue());
      }
    }
  }

  private void queryString() {
    QueryStringDecoder        queryStringDecoder = new QueryStringDecoder(request.uri());
    Map<String, List<String>> params             = queryStringDecoder.parameters();
    if (Objects.isNull(params) || params.isEmpty()) {
      return;
    }
    for (Map.Entry<String, List<String>> p : params.entrySet()) {
      String       key   = p.getKey();
      List<String> value = p.getValue();
      this.queryString.put(key, value.get(0));
    }
  }

  private void cookies() {
    String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
    if (cookieString != null) {
      Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
      if (cookies.isEmpty()) {
        return;
      }
      for (Cookie cookie : cookies) {
        this.cookies.put(cookie.name(), cookie.value());
      }
    }
  }

  private void headers() {
    HttpHeaders                     httpHeaders = request.headers();
    List<Map.Entry<String, String>> entries     = httpHeaders.entries();
    if (entries.isEmpty()) {
      return;
    }
    for (Map.Entry<String, String> h : entries) {
      CharSequence key   = h.getKey();
      CharSequence value = h.getValue();
      headers.put(key.toString(), value);
    }
  }

  public Map<String, Object> getCookies() {
    return cookies;
  }

  public Map<String, Object> getHeaders() {
    return headers;
  }

  public Map<String, String> getQueryString() {
    return queryString;
  }

  public Map<String, Object> getFormData() {
    return formData;
  }

  public Map<String, Object> getJsonData() {
    return jsonData;
  }

  public String getUri() {
    return uri;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getVersion() {
    return version;
  }

  public void setCookies(Map<String, Object> cookies) {
    this.cookies = cookies;
  }

  public void setHeaders(Map<String, Object> headers) {
    this.headers = headers;
  }

  public void setQueryString(Map<String, String> queryString) {
    this.queryString = queryString;
  }

  public void setFormData(Map<String, Object> formData) {
    this.formData = formData;
  }

  public void setJsonData(Map<String, Object> jsonData) {
    this.jsonData = jsonData;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public FullHttpResponse getResponse() {
    return response;
  }

  public void setResponse(FullHttpResponse response) {
    this.response = response;
  }

  public FullHttpRequest getRequest() {
    return request;
  }
}
