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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aquiver.PathVarMatcher;
import org.aquiver.RequestContext;

import java.util.Collection;
import java.util.Map;

public final class ParameterDispenser {

  public static Object dispen(RequestHandlerParam handlerParam, RequestContext requestContext, String url) {
    switch (handlerParam.getType()) {
      case REQUEST_PARAM:
        return getQueryString(handlerParam, requestContext);
      case REQUEST_COOKIES:
        return getRequestCookies(handlerParam, requestContext);
      case REQUEST_HEADER:
        return getRequestHeaders(handlerParam, requestContext);
      case PATH_VARIABLE:
        return getPathVariable(handlerParam, requestContext, url);
      case REQUEST_BODY:
        return getRequestBody(handlerParam, requestContext);
      default:
        break;
    }
    return null;
  }

  private static Object getQueryString(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getQueryString();
    }
    return handlerParam.getDataType().cast(requestContext.getQueryString().get(handlerParam.getName()));
  }

  private static Object getRequestCookies(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getCookies();
    }
    return handlerParam.getDataType().cast(requestContext.getCookies().get(handlerParam.getName()));
  }

  private static Object getRequestHeaders(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getHeaders();
    }
    return handlerParam.getDataType().cast(requestContext.getHeaders().get(handlerParam.getName()));
  }

  private static Object getPathVariable(RequestHandlerParam handlerParam, RequestContext requestContext, String url) {
    return handlerParam.getDataType().cast(
            PathVarMatcher.getPathVariable(requestContext.getUri(), url, handlerParam.getName()));
  }

  private static Object getRequestBody(RequestHandlerParam handlerParam, RequestContext requestContext) {
    Map<String, Object> jsonData = requestContext.getJsonData();
    String jsonString = JSON.toJSONString(jsonData);
    if (jsonData.isEmpty() || !JSONObject.isValid(jsonString)) {
      return null;
    }
    return JSONObject.parseObject(jsonString, handlerParam.getDataType());
  }

  public static boolean isCollection(Class<?> cls) {
    return Collection.class.isAssignableFrom(cls);
  }

  public static boolean isMap(Class<?> cls) {
    return Map.class.isAssignableFrom(cls);
  }
}
