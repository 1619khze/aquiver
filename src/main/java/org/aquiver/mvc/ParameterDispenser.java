package org.aquiver.mvc;

import org.aquiver.Request;

public final class ParameterDispenser {

  public static Object dispen(RequestHandlerParam handlerParam, Request request, String url) {
    switch (handlerParam.getType()) {
      case REQUEST_PARAM:
        return getQueryString(handlerParam, request);
      case REQUEST_COOKIES:
        return getRequestCookies(handlerParam, request);
      case REQUEST_HEADER:
        return getRequestHeaders(handlerParam, request);
      case PATH_VARIABLE:
        return getPathVariable(handlerParam, request,url);
      case REQUEST_BODY:
        return getRequestBody(handlerParam, request);
      case HTTP_RESPONSE:
    }
    return null;
  }

  private static Object getQueryString(RequestHandlerParam handlerParam, Request request) {
    return handlerParam.getDataType().cast(request.getQueryString().get(handlerParam.getName()));
  }

  private static Object getRequestCookies(RequestHandlerParam handlerParam, Request request) {
    return handlerParam.getDataType().cast(request.getCookies().get(handlerParam.getName()));
  }

  private static Object getRequestHeaders(RequestHandlerParam handlerParam, Request request) {
    return handlerParam.getDataType().cast(request.getHeaders().get(handlerParam.getName()));
  }

  private static Object getPathVariable(RequestHandlerParam handlerParam, Request request, String url) {
    return handlerParam.getDataType().cast(getPathVariable(request.getUri(), url, handlerParam.getName()));
  }

  private static Object getRequestBody(RequestHandlerParam handlerParam, Request request) {
    return null;
  }

  private static String getPathVariable(String url, String mappingUrl, String name) {
    String[] urlSplit = url.split("/");
    String[] mappingUrlSplit = mappingUrl.split("/");
    for (int i = 0; i < mappingUrlSplit.length; i++) {
      if (mappingUrlSplit[i].equals("{" + name + "}")) {
        if(urlSplit[i].contains("?")) {
          return urlSplit[i].split("[?]")[0];
        }
        if(urlSplit[i].contains("&")) {
          return urlSplit[i].split("&")[0];
        }
        return urlSplit[i];
      }
    }
    return null;
  }
}
