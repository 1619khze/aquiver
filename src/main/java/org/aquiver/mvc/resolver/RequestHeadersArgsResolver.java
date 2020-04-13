package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.RequestHeader;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

public class RequestHeadersArgsResolver implements ArgsResolver {

  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    if (parameter.getAnnotation(RequestHeader.class) != null) {
      RequestHandlerParam handlerParam  = new RequestHandlerParam();
      RequestHeader       requestHeader = parameter.getAnnotation(RequestHeader.class);
      handlerParam.setDataType(parameter.getType());
      handlerParam.setName("".equals(requestHeader.value()) ? paramName : requestHeader.value());
      handlerParam.setRequired(true);
      handlerParam.setType(RequestParamType.REQUEST_HEADER);
      return handlerParam;
    }
    return null;
  }
}
