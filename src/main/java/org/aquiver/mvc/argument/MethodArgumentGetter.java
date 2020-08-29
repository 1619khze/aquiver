package org.aquiver.mvc.argument;

import org.apex.ApexContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/8/28
 */
public class MethodArgumentGetter {
  private final AnnotationArgumentGetterResolver annotationResolver;
  private final ArgumentGetterResolver argumentGetterResolver;
  private ArgumentGetterContext argumentGetterContext;

  public void setArgumentGetterContext(ArgumentGetterContext argumentGetterContext) {
    this.argumentGetterContext = argumentGetterContext;
  }

  public MethodArgumentGetter() {
    final ApexContext context = ApexContext.of();
    this.annotationResolver = context.getBean(AnnotationArgumentGetterResolver.class);
    this.argumentGetterResolver = context.getBean(ArgumentGetterResolver.class);
  }

  public Object getParam(Parameter parameter) throws Exception {
    if (parameter.getAnnotations().length == 0) {
      ArgumentGetter<?> lookup = argumentGetterResolver.lookup(parameter.getType());
      return lookup.get(argumentGetterContext);
    } else {
      Annotation[] annotations = parameter.getAnnotations();
      for (Annotation annotation : annotations) {
        AnnotationArgumentGetter lookup = annotationResolver.lookup(annotation.annotationType());
        ArgumentContext argumentContext = new ArgumentContext(parameter, annotation, argumentGetterContext);
        return lookup.get(argumentContext);
      }
    }
    return null;
  }
}
