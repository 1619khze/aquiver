package org.aquiver.route.resolver;

import org.aquiver.ParamResolver;
import org.aquiver.RequestContext;
import org.aquiver.route.RouteParam;
import org.aquiver.route.RouteParamType;
import org.aquiver.route.session.Session;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/6/28
 */
public class SessionParamResolver extends AbstractParamResolver implements ParamResolver {

  @Override
  public boolean support(Parameter parameter) {
    return parameter.getType().isAssignableFrom(Session.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("");
    handlerParam.setRequired(true);
    handlerParam.setType(RouteParamType.REQUEST_SESSION);
    return handlerParam;
  }

  @Override
  public Object dispen(RouteParam handlerParam, RequestContext requestContext, String url) {
    return handlerParam.getDataType().cast(requestContext.request().session());
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.REQUEST_SESSION;
  }
}
