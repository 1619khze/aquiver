package org.aquiver;

import java.util.Collection;

@FunctionalInterface
public interface Discoverer {
  Collection<Class<?>> discover(String scanPath);
}
