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

import org.aquiver.mvc.annotation.bind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/26
 */
public final class AnnotationArgumentGetterResolver implements GetterResolver<AnnotationArgumentGetter> {
  private static final Logger log = LoggerFactory.getLogger(AnnotationArgumentGetterResolver.class);
  private final Map<Class<?>, AnnotationArgumentGetter> mapping = new HashMap<>();

  public AnnotationArgumentGetterResolver() {
    init();
  }

  @Override
  public void init() {
    // Register all Getters that inject parameters through annotations
    this.registerArgumentGetter(Param.class, ParamArgumentGetter.class);
    this.registerArgumentGetter(Body.class, BodyArgumentGetter.class);
    this.registerArgumentGetter(Cookies.class, CookiesArgumentGetter.class);
    this.registerArgumentGetter(Header.class, HeaderArgumentGetter.class);
    this.registerArgumentGetter(PathVar.class, PathVarArgumentGetter.class);
    this.registerArgumentGetter(FileUpload.class, FileUploadArgumentGetter.class);
    this.registerArgumentGetter(MultiFileUpload.class, MultiFileUploadArgumentGetter.class);
  }

  @Override
  public void registerArgumentGetter(Class<?> bindClass, Class<?> argumentClass) {
    Objects.requireNonNull(bindClass, "bindClass can't be null");
    Objects.requireNonNull(argumentClass, "argumentClass can't be null");

    if (!Annotation.class.isAssignableFrom(bindClass)) {
      throw new IllegalArgumentException(bindClass.getName() + " cam't assignable from Annotation");
    }

    if (!AnnotationArgumentGetter.class.isAssignableFrom(argumentClass)) {
      throw new IllegalArgumentException(bindClass.getName() + " cam't assignable from AnnotationArgumentGetter");
    }

    AnnotationArgumentGetter annotationArgumentGetter = (AnnotationArgumentGetter) apexContext().addBean(argumentClass);
    this.mapping.put(bindClass, annotationArgumentGetter);
  }

  @Override
  public AnnotationArgumentGetter lookup(Class<?> bindClass) {
    Objects.requireNonNull(bindClass, "bindClass can't be null");
    if (!Annotation.class.isAssignableFrom(bindClass)) {
      throw new IllegalArgumentException(bindClass.getName() + "can't assignable from Annotation");
    }
    return mapping.getOrDefault(bindClass, null);
  }
}
