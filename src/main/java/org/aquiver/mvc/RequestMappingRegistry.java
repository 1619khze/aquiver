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

  private List<ArgsResolver>  argsResolvers  = new ArrayList<>();
  private List<ArgsConverter<?>> argsConverters = new ArrayList<>();

  public List<ArgsResolver> getArgsResolvers() {
    return argsResolvers;
  }

  public void setArgsResolvers(List<ArgsResolver> argsResolvers) {
    this.argsResolvers = argsResolvers;
  }

  public List<ArgsConverter<?>> getArgsConverters() {
    return argsConverters;
  }

  public void setArgsConverters(List<ArgsConverter<?>> argsConverters) {
    this.argsConverters = argsConverters;
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
            Objects.isNull(method.getAnnotation(ResponseBody.class)), requestMethod);

    Parameter[] ps = method.getParameters();

    try {
      String[] paramNames = this.getMethodParameterNamesByAsm(clazz, method);
      for (int i = 0; i < ps.length; i++) {
        List<ArgsResolver> argsResolvers = getArgsResolvers();
        for (ArgsResolver argsResolver : argsResolvers) {
          RequestHandlerParam param = argsResolver.resolve(ps[i].getType(), paramNames[i]);
          requestHandler.getParams().add(param);
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
