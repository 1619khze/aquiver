package org.aquiver.mvc.resolver;

import org.aquiver.RequestContext;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.mvc.route.RouteParamType;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/7/4
 */
public class RequestContextParamResolver implements ParamResolver {
  @Override
  public boolean support(Parameter parameter) {
    return parameter.getType().isAssignableFrom(RequestContext.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("");
    handlerParam.setRequired(true);
    handlerParam.setType(RouteParamType.REQUEST_CONTEXT);
    return handlerParam;
  }

  @Override
  public Object dispen(Class<?> paramType, String paramName, RequestContext requestContext) {
    return paramType.cast(requestContext);
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.REQUEST_CONTEXT;
  }
}
