package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.Body;
import org.aquiver.annotation.bind.Cookies;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/5/28
 */
public class RequestBodyArgsResolver implements ArgsResolver {
  @Override
  public boolean support(Parameter parameter) {
    return parameter.isAnnotationPresent(Body.class);
  }

  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    RequestHandlerParam handlerParam = new RequestHandlerParam();
    Body body = parameter.getAnnotation(Body.class);
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("".equals(body.value()) ? paramName : body.value());
    handlerParam.setRequired(true);
    handlerParam.setType(RequestParamType.REQUEST_BODY);
    return handlerParam;
  }
}
