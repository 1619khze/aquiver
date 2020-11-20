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
package org.aquiver.mvc.argument;

import org.apex.ApexContext;
import org.aquiver.RequestContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/28
 */
public class MethodArgumentGetter {
  private final AnnotationArgumentGetterResolver annotationResolver;
  private final ArgumentGetterResolver argumentGetterResolver;
  private final RequestContext requestContext;

  public MethodArgumentGetter(RequestContext requestContext) {
    this.annotationResolver = new AnnotationArgumentGetterResolver();
    this.argumentGetterResolver = new ArgumentGetterResolver();
    this.requestContext = requestContext;
  }

  public Object getParam(Parameter parameter) throws Exception {
    if (parameter.getAnnotations().length == 0) {
      ArgumentGetter<?> lookup = argumentGetterResolver.lookup(parameter.getType());
      return lookup.get(requestContext);
    } else {
      Annotation[] annotations = parameter.getAnnotations();
      for (Annotation annotation : annotations) {
        AnnotationArgumentGetter lookup = annotationResolver.lookup(annotation.annotationType());
        ArgumentContext argumentContext = new ArgumentContext(parameter, annotation, requestContext);
        return lookup.get(argumentContext);
      }
    }
    return null;
  }

  public List<Object> getParams(Parameter[] parameters) throws Exception {
    final List<Object> invokeArguments = new ArrayList<>(parameters.length);
    for (Parameter parameter : parameters) {
      Object param = this.getParam(parameter);
      if (Objects.isNull(param)) {
        param = new Object();
      }
      invokeArguments.add(param);
    }
    return invokeArguments;
  }
}
