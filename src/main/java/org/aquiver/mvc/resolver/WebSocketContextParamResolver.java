package org.aquiver.mvc.resolver;

import org.aquiver.RequestContext;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.mvc.route.RouteParamType;
import org.aquiver.websocket.WebSocketContext;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/7/13
 */
public class WebSocketContextParamResolver implements ParamResolver{
  @Override
  public boolean support(Parameter parameter) {
    return parameter.getType().isAssignableFrom(WebSocketContext.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("");
    handlerParam.setRequired(true);
    handlerParam.setType(RouteParamType.WEBSOCKET_CONTEXT);
    return handlerParam;
  }

  @Override
  public Object dispen(Class<?> paramType, String paramName, RequestContext requestContext) {
    return requestContext.webSocketContext();
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.WEBSOCKET_CONTEXT;
  }
}
