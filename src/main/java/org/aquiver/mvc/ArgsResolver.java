package org.aquiver.mvc;

import java.lang.reflect.Parameter;

@FunctionalInterface
public interface ArgsResolver {
  RequestHandlerParam resolve(Parameter parameter, String paramName);
}
