package org.aquiver.toolkit;

import java.lang.reflect.Modifier;

public abstract class Reflections {

  public static <T> boolean isInterface(Class<T> clazz) {
    return clazz.isInterface();
  }

  public static <T> boolean isAbstract(Class<T> clazz) {
    int modifiers = clazz.getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  public static <T> boolean isEnum(Class<T> clazz) {
    return clazz.isEnum();
  }

  public static <T> boolean isNormal(Class<T> clazz) {
    return !isAbstract(clazz) && !isInterface(clazz) && !isEnum(clazz);
  }
}
