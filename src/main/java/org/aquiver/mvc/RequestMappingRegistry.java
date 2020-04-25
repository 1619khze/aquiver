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

import org.aquiver.AbstractRegistry;
import org.aquiver.HandlerRepeatException;
import org.aquiver.annotation.RequestMapping;
import org.aquiver.annotation.RequestMethod;
import org.aquiver.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestMappingRegistry extends AbstractRegistry {

  private static final Logger log = LoggerFactory.getLogger(RequestMappingRegistry.class);

  private List<ArgsResolver> argsResolvers = new ArrayList<>();

  public List<ArgsResolver> getArgsResolvers() {
    return argsResolvers;
  }

  public void setArgsResolvers(List<ArgsResolver> argsResolvers) {
    this.argsResolvers = argsResolvers;
  }

  @Override
  public void register(Class<?> clazz, String baseUrl, Method method, RequestMapping methodRequestMapping) {
    String methodUrl = methodRequestMapping.value();
    String url       = super.getMethodUrl(baseUrl, methodUrl);

    RequestMethod requestMethod = methodRequestMapping.method();
    if (url.trim().isEmpty()) {
      return;
    }

    RequestHandler requestHandler = new RequestHandler(url, clazz, method.getName(),
            !Objects.isNull(method.getAnnotation(ResponseBody.class)), requestMethod);

    Parameter[] ps = method.getParameters();

    try {
      String[] paramNames = this.getMethodParameterNamesByAsm(clazz, method);
      for (int i = 0; i < ps.length; i++) {
        List<ArgsResolver> argsResolvers = getArgsResolvers();
        if (argsResolvers.isEmpty()) {
          break;
        }
        for (ArgsResolver argsResolver : argsResolvers) {
          Parameter parameter = ps[i];
          if (!argsResolver.support(parameter)) {
            continue;
          }
          RequestHandlerParam param = argsResolver.resolve(parameter, paramNames[i]);
          if (param != null) {
            requestHandler.getParams().add(param);
          }
        }
      }
    } catch (IOException e) {
      log.error("An exception occurred while parsing the method parameter name", e);
    }

    if (this.repeat(url)) {
      if (log.isDebugEnabled()) {
        log.debug("Registered request processor URL is duplicated :{}", url);
      }
      throw new HandlerRepeatException("Registered request processor URL is duplicated : " + url);
    }
    this.register(url, requestHandler);
  }

  @Override
  public Map<String, RequestHandler> getRequestHandlers() {
    return super.getRequestHandlers();
  }
}
