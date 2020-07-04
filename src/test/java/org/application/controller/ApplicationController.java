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
package org.application.controller;

import org.application.bean.User;
import org.aquiver.mvc.annotation.*;
import org.aquiver.mvc.annotation.bind.*;
import org.aquiver.mvc.route.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

@RestPath
@Path(value = "/controller")
public class ApplicationController {

  private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

  @Path(value = "/requestParam", method = PathMethod.GET)
  public String requestParam(@Param String name) {
    log.info("request param:" + name);
    return "requestParam:" + name;
  }

  @JSON
  @Path(value = "/requestParamJson", method = PathMethod.GET)
  public User requestParamJson(@Param String name) {
    log.info("request param:" + name);
    return new User("1", 2, (short) 3);
  }

  @Path(value = "/requestParamAlisa", method = PathMethod.GET)
  public String requestParamAlisa(@Param(value = "username") String name) {
    log.info("request param:" + name);
    return "requestParamAlisa:" + name;
  }

  @Path(value = "/requestCookies", method = PathMethod.GET)
  public String requestCookies(@Cookies String name) {
    log.info("request param:" + name);
    return "requestCookies:" + name;
  }

  @Path(value = "/requestHeaders", method = PathMethod.GET)
  public String requestHeaders(@Header(value = "Accept") String name) {
    log.info("request param:" + name);
    return "requestHeaders:" + name;
  }

  @Path(value = "/pathVariable/{name}/{code}", method = PathMethod.GET)
  public String pathVariable(@PathVar String name, @PathVar String code) {
    log.info("request param:" + name + ":" + "request param:" + code);
    return "pathVariable:" + name + ":" + code;
  }

  @Path(value = "/postBody", method = PathMethod.POST)
  public String postBody(@Body User user) {
    log.info("post body param:" + user);
    return "post body:" + user;
  }

  @GET(value = "/get")
  public String get() {
    return "controller/get";
  }

  @POST(value = "/uploadFile")
  public String uploadFile(@FileUpload MultipartFile file) {
    log.info("fileName:{}", file.fileName());
    try {
      System.out.println(file.readFileContent());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "controller/uploadFile";
  }

  @POST(value = "/uploadFiles")
  public String uploadFileS(@MultiFileUpload List<MultipartFile> files) {
    log.info("file size:{}", files.size());
    for (MultipartFile multipartFile : files) {
      log.info("fileName:{}", multipartFile.fileName());
      try {
        System.out.println(multipartFile.readFileContent());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return "controller/uploadFiles";
  }
}
