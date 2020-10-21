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
package org.aquiver.mvc.argument;

import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.RequestContext;
import org.aquiver.mvc.router.multipart.MultipartFile;
import org.aquiver.mvc.router.multipart.MultipartFileUtils;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/26
 */
public final class FileUploadArgumentGetter implements AnnotationArgumentGetter {
  @Override
  public Object get(ArgumentContext context) throws Exception {
    RequestContext requestContext = context.getContext();
    Map<String, FileUpload> fileUploads = requestContext.request().fileUploads();
    Parameter parameter = context.getParameter();
    String name = parameter.getName();
    if (MultipartFile.class.isAssignableFrom(parameter.getType()) && fileUploads.containsKey(name)) {
      io.netty.handler.codec.http.multipart.FileUpload fileUpload = fileUploads.get(name);
      return MultipartFileUtils.createMultipartFile(fileUpload, requestContext.channelContext());
    }
    return null;
  }
}
