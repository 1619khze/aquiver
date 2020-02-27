package org.aquiver;

import com.google.inject.AbstractModule;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Function;

public class AutoScanModule extends AbstractModule {
  private final Reflections                      packageReflections;
  private final Set<Class<? extends Annotation>> bindingAnnotations;

  public AutoScanModule(Reflections packageReflections, final Set<Class<? extends Annotation>> bindingAnnotations) {
    this.packageReflections = packageReflections;
    this.bindingAnnotations = bindingAnnotations;
  }

  @Override
  public void configure() {
    this.bindingAnnotations.stream()
            .map(getClassSetFunction())
            .flatMap(Set::stream)
            .forEach(this::bind);
  }

  private Function<Class<? extends Annotation>, Set<Class<?>>> getClassSetFunction() {
    return bindingAnnotation -> packageReflections.getTypesAnnotatedWith(bindingAnnotation, true);
  }
}
