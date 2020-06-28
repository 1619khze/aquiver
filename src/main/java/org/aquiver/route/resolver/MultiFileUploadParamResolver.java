/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.route.resolver;

import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.ParamResolver;
import org.aquiver.RequestContext;
import org.aquiver.annotation.bind.MultiFileUpload;
import org.aquiver.route.RouteParam;
import org.aquiver.route.RouteParamType;
import org.aquiver.route.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/6/14
 */
public class MultiFileUploadParamResolver extends AbstractParamResolver implements ParamResolver {

  @Override
  public boolean support(Parameter parameter) {
    return parameter.isAnnotationPresent(MultiFileUpload.class) &&
            parameter.getType().isAssignableFrom(List.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    MultiFileUpload param = parameter.getAnnotation(MultiFileUpload.class);
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName("".equals(param.value()) ? paramName : param.value());
    handlerParam.setType(RouteParamType.UPLOAD_FILES);
    return handlerParam;
  }

  @Override
  public Object dispen(RouteParam handlerParam, RequestContext requestContext, String url) throws Exception {
    Map<String, FileUpload> fileUploads = requestContext.request().fileUpload();
    List<MultipartFile> multipartFiles = new ArrayList<>();
    if (List.class.isAssignableFrom(handlerParam.getDataType())) {
      for (Map.Entry<String, FileUpload> entry : fileUploads.entrySet()) {
        FileUpload value = entry.getValue();
        MultipartFile multipartFile = buildMultipartFile(value);
        multipartFiles.add(multipartFile);
      }
    }
    return multipartFiles;
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.UPLOAD_FILES;
  }
}
