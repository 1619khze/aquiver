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
package org.aquiver.mvc.cors;

import org.aquiver.mvc.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author WangYi
 * @since 2020/10/7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CorsDecorator {
  /**
   * Allowed origins.
   * Sets this property to be {@code "*"} to allow any origin.
   */
  String[] origins();

  /**
   * The path patterns that this policy is supposed to be applied to. If unspecified, all paths would be
   * accepted.
   */
  String[] pathPatterns() default {};

  /**
   * The allowed HTTP request methods that should be returned in the
   * CORS {@code "Access-Control-Allow-Methods"} response header.
   *
   * @see CorsPolicyBuilder#allowRequestMethods(HttpMethod...)
   */
  HttpMethod[] allowedRequestMethods() default {};

  /**
   * The value of the CORS {@code "Access-Control-Max-Age"} response header which enables the
   * caching of the preflight response for the specified time. During this time no preflight
   * request will be made.
   *
   * @see CorsPolicyBuilder#maxAge(long)
   */
  long maxAge() default 0;

  /**
   * The headers to be exposed to calling clients.
   *
   * @see CorsPolicyBuilder#exposeHeaders(CharSequence...)
   */
  String[] exposedHeaders() default {};

  /**
   * The headers that should be returned in the CORS {@code "Access-Control-Allow-Headers"}
   * response header.
   *
   * @see CorsPolicyBuilder#allowRequestHeaders(CharSequence...)
   */
  String[] allowedRequestHeaders() default {};

  /**
   * Determines if cookies are allowed to be added to CORS requests.
   * Settings this to be {@code true} will set the CORS {@code "Access-Control-Allow-Credentials"}
   * response header to {@code "true"}.
   *
   * <p>If unset, will be {@code false}.
   *
   * @see CorsPolicyBuilder#allowCredentials()
   */
  boolean credentialsAllowed() default false;

  /**
   * Determines if a {@code "null"} origin is allowed.
   *
   * <p>If unset, will be {@code false}.
   *
   * @see CorsPolicyBuilder#allowNullOrigin()
   */
  boolean nullOriginAllowed() default false;

  /**
   * Determines if no preflight response headers should be added to a CORS preflight response.
   *
   * <p>If unset, will be {@code false}.
   *
   * @see CorsPolicyBuilder#disablePreflightResponseHeaders()
   */
  boolean preflightRequestDisabled() default false;
}
