package org.aquiver;

import java.util.List;

@FunctionalInterface
public interface Discoverer {
  List<Class<?>> discover(String scanPath);
}
