package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.PathVariable;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

public class PathVariableArgsResolver implements ArgsResolver {

  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    if (parameter.getAnnotation(PathVariable.class) != null) {
      RequestHandlerParam handlerParam = new RequestHandlerParam();
      PathVariable        pathVariable = parameter.getAnnotation(PathVariable.class);
      handlerParam.setDataType(parameter.getType());
      handlerParam.setName((!"".equals(pathVariable.value())&& !pathVariable.value().trim().isEmpty()) ?
              pathVariable.value().trim() : paramName);
      handlerParam.setRequired(true);
      handlerParam.setType(RequestParamType.PATH_VARIABLE);
      return handlerParam;
    }
    return null;
  }
}
