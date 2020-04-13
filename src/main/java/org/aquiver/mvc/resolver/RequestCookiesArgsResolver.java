package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.RequestCookies;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

public class RequestCookiesArgsResolver implements ArgsResolver {

  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    if (parameter.getAnnotation(RequestCookies.class) != null) {
      RequestHandlerParam handlerParam   = new RequestHandlerParam();
      RequestCookies      requestCookies = parameter.getAnnotation(RequestCookies.class);
      handlerParam.setDataType(parameter.getType());
      handlerParam.setName("".equals(requestCookies.value()) ? paramName : requestCookies.value());
      handlerParam.setRequired(true);
      handlerParam.setType(RequestParamType.REQUEST_COOKIES);
      return handlerParam;
    }
    return null;
  }
}
