package org.aquiver.mvc.resolver;

import org.aquiver.annotation.bind.FileUpload;
import org.aquiver.mvc.ArgsResolver;
import org.aquiver.mvc.RequestHandlerParam;
import org.aquiver.mvc.RequestParamType;

import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/6/14
 */
public class FileUploadArgsResolver implements ArgsResolver {
  @Override
  public boolean support(Parameter parameter) {
    return parameter.isAnnotationPresent(FileUpload.class);
  }

  @Override
  public RequestHandlerParam resolve(Parameter parameter, String paramName) {
    RequestHandlerParam handlerParam = new RequestHandlerParam();
    FileUpload param = parameter.getAnnotation(FileUpload.class);
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("".equals(param.value()) ? paramName : param.value());
    handlerParam.setType(RequestParamType.UPLOAD_FILE);
    return handlerParam;
  }
}
