package org.aquiver;

@FunctionalInterface
public interface Discoverer<T> {
  T discover(String scanPath);
}
