package org.aquiver.mvc.argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public final class ArgumentContext {
  private final Parameter parameter;
  private final Annotation annotation;
  private final ArgumentGetterContext context;

  public ArgumentContext(Parameter parameter, Annotation annotation, ArgumentGetterContext context) {
    this.parameter = parameter;
    this.annotation = annotation;
    this.context = context;
  }

  public Parameter getParameter() {
    return parameter;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public ArgumentGetterContext getContext() {
    return context;
  }
}
