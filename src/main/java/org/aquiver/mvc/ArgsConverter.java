package org.aquiver.mvc;

import org.aquiver.Request;

public interface ArgsConverter<T> {

  boolean support(RequestParamType paramType);

  T converter(Request request);
}