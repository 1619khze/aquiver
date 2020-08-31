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

import org.aquiver.RequestContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public final class ArgumentContext {
  private final Parameter parameter;
  private final Annotation annotation;
  private final RequestContext context;

  public ArgumentContext(Parameter parameter, Annotation annotation, RequestContext requestContext) {
    this.parameter = parameter;
    this.annotation = annotation;
    this.context = requestContext;
  }

  public Parameter getParameter() {
    return parameter;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public RequestContext getContext() {
    return context;
  }
}
