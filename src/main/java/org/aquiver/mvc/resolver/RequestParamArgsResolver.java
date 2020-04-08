package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.RequestParam;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

public class RequestParamArgsResolver implements ArgsResolver {
  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    if (parameter.getAnnotation(RequestParam.class) != null) {
      RequestHandlerParam handlerParam =new RequestHandlerParam();
      RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
      handlerParam.setDataType(parameter.getType());
      handlerParam.setName("".equals(requestParam.value()) ? paramName : requestParam.value());
      handlerParam.setRequired(requestParam.required());
      handlerParam.setType(RequestParamType.REQUEST_PARAM);
      return handlerParam;
    }
    return null;
  }
}
