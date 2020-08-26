/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.utils;

import org.aquiver.mvc.argument.ArgumentGetterContext;
import org.aquiver.mvc.argument.ArgumentResolverManager;
import org.aquiver.mvc.router.RouteParam;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtils {
  private ReflectionUtils() {
  }

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

  public static void invokeParam(ArgumentGetterContext context, List<RouteParam> routeParams,
                                 Object[] paramValues, Class<?>[] paramTypes,
                                 ArgumentResolverManager resolverManager) {
    for (int i = 0; i < paramValues.length; i++) {
      RouteParam handlerParam = routeParams.get(i);
      paramTypes[i] = handlerParam.getDataType();
      try {
        paramValues[i] = resolverManager.assignment(
                handlerParam, context);
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static Method getInvokeMethod(Class<?> refClass, String method,
                                       Class<?>[] paramTypes) {
    try {
      return refClass.getMethod(method, paramTypes);
    }
    catch(NoSuchMethodException e) {
      return null;
    }
  }

  public static String[] getMethodParamName(Method method) {
    Parameter[] parameters = method.getParameters();
    List<String> nameList = new ArrayList<>();
    for (Parameter param : parameters) {
      String name = param.getName();
      nameList.add(name);
    }
    return nameList.toArray(new String[0]);
  }
}
