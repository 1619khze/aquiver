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
package org.aquiver.mvc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.PathVarMatcher;
import org.aquiver.RequestContext;
import org.aquiver.mvc.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ParameterDispenser {
  private static final Logger log = LoggerFactory.getLogger(ParameterDispenser.class);

  public static Object dispen(RequestHandlerParam handlerParam, RequestContext requestContext, String url) {
    switch (handlerParam.getType()) {
      case REQUEST_PARAM:
        return getQueryString(handlerParam, requestContext);
      case REQUEST_COOKIES:
        return getRequestCookies(handlerParam, requestContext);
      case REQUEST_HEADER:
        return getRequestHeaders(handlerParam, requestContext);
      case PATH_VARIABLE:
        return getPathVariable(handlerParam, requestContext, url);
      case REQUEST_BODY:
        return getRequestBody(handlerParam, requestContext);
      case UPLOAD_FILE:
        try {
          return getUploadFile(handlerParam, requestContext);
        } catch (Exception e) {
          e.printStackTrace();
        }
      case UPLOAD_FILES:
        try {
          return getUploadFiles(handlerParam, requestContext);
        } catch (Exception e) {
          e.printStackTrace();
        }
      default:
        break;
    }
    return null;
  }

  private static Object getUploadFiles(RequestHandlerParam handlerParam, RequestContext requestContext) throws IOException {
    Map<String, FileUpload> fileUploads = requestContext.getFileUploads();
    List<MultipartFile> multipartFiles = new ArrayList<>();
    if (List.class.isAssignableFrom(handlerParam.getDataType())) {
      for (Map.Entry<String, FileUpload> entry : fileUploads.entrySet()) {
        FileUpload value = entry.getValue();
        MultipartFile multipartFile = buildMultipartFile(handlerParam, value);
        multipartFiles.add(multipartFile);
      }
    }
    return multipartFiles;
  }

  private static Object getUploadFile(RequestHandlerParam handlerParam, RequestContext requestContext) throws Exception {
    Map<String, FileUpload> fileUploads = requestContext.getFileUploads();
    if (MultipartFile.class.isAssignableFrom(handlerParam.getDataType()) &&
            fileUploads.containsKey(handlerParam.getName())) {
      FileUpload fileUpload = fileUploads.get(handlerParam.getName());
      return buildMultipartFile(handlerParam, fileUpload);
    }
    return null;
  }

  private static MultipartFile buildMultipartFile(RequestHandlerParam handlerParam,
                                                  FileUpload fileUpload) throws IOException {
    MultipartFile multipartFile = new MultipartFile();
    multipartFile.setCharset(fileUpload.getCharset());
    multipartFile.setCharsetName(fileUpload.getCharset().name());
    multipartFile.setContentTransferEncoding(fileUpload.getContentTransferEncoding());
    multipartFile.setContentType(fileUpload.getContentType());
    multipartFile.setFileName(fileUpload.getFilename());

    Path tmpFile = Files.createTempFile(
            Paths.get(fileUpload.getFile().getParent()), "aquiver", "_upload");

    Path fileUploadPath = Paths.get(fileUpload.getFile().getPath());
    Files.move(fileUploadPath, tmpFile, StandardCopyOption.REPLACE_EXISTING);

    multipartFile.setFile(tmpFile.toFile());
    multipartFile.setInputStream(new FileInputStream(multipartFile.getFile()));
    multipartFile.setPath(tmpFile.toFile().getPath());
    multipartFile.setLength(fileUpload.length());
    return multipartFile;
  }

  private static Object getQueryString(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getQueryString();
    }
    return handlerParam.getDataType().cast(requestContext.getQueryString().get(handlerParam.getName()));
  }

  private static Object getRequestCookies(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getCookies();
    }
    return handlerParam.getDataType().cast(requestContext.getCookies().get(handlerParam.getName()));
  }

  private static Object getRequestHeaders(RequestHandlerParam handlerParam, RequestContext requestContext) {
    if (isMap(handlerParam.getDataType())) {
      return requestContext.getHeaders();
    }
    return handlerParam.getDataType().cast(requestContext.getHeaders().get(handlerParam.getName()));
  }

  private static Object getPathVariable(RequestHandlerParam handlerParam, RequestContext requestContext, String url) {
    return handlerParam.getDataType().cast(
            PathVarMatcher.getPathVariable(requestContext.getUri(), url, handlerParam.getName()));
  }

  private static Object getRequestBody(RequestHandlerParam handlerParam, RequestContext requestContext) {
    Map<String, Object> jsonData = requestContext.getFormData();
    String jsonString = JSON.toJSONString(jsonData);
    if (jsonData.isEmpty() || !JSONObject.isValid(jsonString)) {
      return null;
    }
    return JSONObject.parseObject(jsonString, handlerParam.getDataType());
  }

  public static boolean isCollection(Class<?> cls) {
    return Collection.class.isAssignableFrom(cls);
  }

  public static boolean isMap(Class<?> cls) {
    return Map.class.isAssignableFrom(cls);
  }
}
