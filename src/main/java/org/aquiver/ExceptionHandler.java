package org.aquiver;

@FunctionalInterface
public interface ExceptionHandler {
  void handler(Exception e);
}
