package org.aquiver;

import org.aquiver.annotation.RequestMapping;

import java.lang.reflect.Method;

@FunctionalInterface
public interface RegisterStrategy {
  void register(Class<?> clazz, String baseUrl, Method method, RequestMapping methodRequestMapping);
}