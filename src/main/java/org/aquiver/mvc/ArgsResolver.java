package org.aquiver.mvc;

@FunctionalInterface
public interface ArgsResolver {
  RequestHandlerParam resolve(Class<?> type, String paramName);
}
