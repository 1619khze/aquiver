package org.aquiver.route.resolver;

import org.aquiver.ParamResolver;
import org.aquiver.RequestContext;
import org.aquiver.route.RouteParam;
import org.aquiver.route.RouteParamType;
import org.aquiver.route.multipart.MultipartFile;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/7/2
 */
public class MultipartFileParamResolver extends AbstractParamResolver implements ParamResolver {

  @Override
  public boolean support(Parameter parameter) {
    return parameter.getType().isAssignableFrom(MultipartFile.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("");
    handlerParam.setRequired(true);
    handlerParam.setType(RouteParamType.MULTIPART_FILE);
    return handlerParam;
  }

  @Override
  public Object dispen(RouteParam handlerParam, RequestContext requestContext, String url) {
    final MultipartFile multipartFile = new MultipartFile();
    multipartFile.channelContext(requestContext.request().channelHandlerContext());
    return handlerParam.getDataType().cast(multipartFile);
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.MULTIPART_FILE;
  }
}
