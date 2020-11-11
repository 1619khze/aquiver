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
package org.aquiver.cors;

import io.netty.util.AsciiString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.aquiver.mvc.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author WangYi
 * @since 2020/10/7
 */
class AbstractCorsPolicyBuilder {
  private final Set<String> origins;
  private boolean credentialsAllowed;
  private boolean nullOriginAllowed;
  private long maxAge;
  private final Set<AsciiString> exposedHeaders = new HashSet<>();
  private final EnumSet<HttpMethod> allowedRequestMethods = EnumSet.noneOf(HttpMethod.class);
  private final Set<AsciiString> allowedRequestHeaders = new HashSet<>();
  private final Map<AsciiString, Supplier<?>> preflightResponseHeaders = new HashMap<>();
  private boolean preflightResponseHeadersDisabled;

  AbstractCorsPolicyBuilder() {
    this.origins = Collections.emptySet();
  }

  AbstractCorsPolicyBuilder(List<String> origins) {
    Validate.notNull(origins, "origins");
    Validate.isTrue(!origins.isEmpty(), "origins is empty.");
    for (int i = 0; i < origins.size(); i++) {
      if (origins.get(i) == null) {
        throw new NullPointerException("origins[" + i + ']');
      }
    }
    this.origins = origins.stream().map(String::toLowerCase).collect(Collectors.toSet());
  }

  public void setConfig(CorsDecorator corsDecorator) {
    if (corsDecorator.credentialsAllowed()) {
      allowCredentials();
    }
    if (corsDecorator.nullOriginAllowed()) {
      allowNullOrigin();
    }
    if (corsDecorator.preflightRequestDisabled()) {
      disablePreflightResponseHeaders();
    }
    if (corsDecorator.exposedHeaders().length > 0) {
      exposeHeaders(corsDecorator.exposedHeaders());
    }
    if (corsDecorator.allowedRequestHeaders().length > 0) {
      allowRequestHeaders(corsDecorator.allowedRequestHeaders());
    }
    if (corsDecorator.allowedRequestMethods().length > 0) {
      allowRequestMethods(corsDecorator.allowedRequestMethods());
    }
    if (corsDecorator.maxAge() > 0) {
      maxAge(corsDecorator.maxAge());
    }
  }

  /**
   * Enables a successful CORS response with a {@code "null"} value for the CORS response header
   * {@code "Access-Control-Allow-Origin"}. Web browsers may set the {@code "Origin"} request header to
   * {@code "null"} if a resource is loaded from the local file system.
   *
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowNullOrigin() {
    nullOriginAllowed = true;
    return this;
  }

  /**
   * Enables cookies to be added to CORS requests.
   * Calling this method will set the CORS {@code "Access-Control-Allow-Credentials"} response header
   * to {@code true}. By default, cookies are not included in CORS requests.
   *
   * <p>Please note that cookie support needs to be enabled on the client side as well.
   * The client needs to opt-in to send cookies by calling:
   * <pre>{@code
   * xhr.withCredentials = true;
   * }</pre>
   *
   * <p>The default value for {@code 'withCredentials'} is {@code false} in which case no cookies are sent.
   * Setting this to {@code true} will include cookies in cross origin requests.
   *
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowCredentials() {
    credentialsAllowed = true;
    return this;
  }

  /**
   * Sets the CORS {@code "Access-Control-Max-Age"} response header and enables the
   * caching of the preflight response for the specified time. During this time no preflight
   * request will be made.
   *
   * @param maxAge the maximum time, in seconds, that the preflight response may be cached.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder maxAge(long maxAge) {
    Validate.isTrue(maxAge > 0, "maxAge: %s (expected: > 0)", maxAge);
    this.maxAge = maxAge;
    return this;
  }

  /**
   * Specifies the headers to be exposed to calling clients.
   *
   * <p>During a simple CORS request, only certain response headers are made available by the
   * browser, for example using:
   * <pre>{@code
   * xhr.getResponseHeader("Content-Type");
   * }</pre>
   *
   * <p>The headers that are available by default are:
   * <ul>
   *   <li>{@code Cache-Control}</li>
   *   <li>{@code Content-Language}</li>
   *   <li>{@code Content-Type}</li>
   *   <li>{@code Expires}</li>
   *   <li>{@code Last-Modified}</li>
   *   <li>{@code Pragma}</li>
   * </ul>
   *
   * <p>To expose other headers they need to be specified which is what this method enables by
   * adding the headers to the CORS {@code "Access-Control-Expose-Headers"} response header.
   *
   * @param headers the values to be added to the {@code "Access-Control-Expose-Headers"} response header
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder exposeHeaders(CharSequence... headers) {
    Validate.notNull(headers, "headers");
    return exposeHeaders(new ArrayList<>(Arrays.asList(headers)));
  }

  /**
   * Specifies the headers to be exposed to calling clients.
   *
   * <p>During a simple CORS request, only certain response headers are made available by the
   * browser, for example using:
   * <pre>{@code
   * xhr.getResponseHeader("Content-Type");
   * }</pre>
   *
   * <p>The headers that are available by default are:
   * <ul>
   *   <li>{@code Cache-Control}</li>
   *   <li>{@code Content-Language}</li>
   *   <li>{@code Content-Type}</li>
   *   <li>{@code Expires}</li>
   *   <li>{@code Last-Modified}</li>
   *   <li>{@code Pragma}</li>
   * </ul>
   *
   * <p>To expose other headers they need to be specified which is what this method enables by
   * adding the headers to the CORS {@code "Access-Control-Expose-Headers"} response header.
   *
   * @param headers the values to be added to the {@code "Access-Control-Expose-Headers"} response header
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder exposeHeaders(List<CharSequence> headers) {
    Validate.notNull(headers, "headers");
    final List<CharSequence> copied = new ArrayList<>(headers);
    Validate.isTrue(!copied.isEmpty(), "headers should not be empty.");
    for (int i = 0; i < copied.size(); i++) {
      if (copied.get(i) == null) {
        throw new NullPointerException("headers[" + i + ']');
      }
    }

    copied.stream().map(AsciiString::of).forEach(this.exposedHeaders::add);
    return this;
  }

  /**
   * Specifies the allowed set of HTTP request methods that should be returned in the
   * CORS {@code "Access-Control-Allow-Methods"} response header.
   *
   * @param methods the {@link HttpMethod}s that should be allowed.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowRequestMethods(HttpMethod... methods) {
    Validate.notNull(methods, "methods");
    return allowRequestMethods(new ArrayList<>(Arrays.asList(methods)));
  }

  /**
   * Specifies the allowed set of HTTP request methods that should be returned in the
   * CORS {@code "Access-Control-Allow-Methods"} response header.
   *
   * @param methods the {@link HttpMethod}s that should be allowed.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowRequestMethods(List<HttpMethod> methods) {
    Validate.notNull(methods, "methods");
    final List<HttpMethod> copied = new ArrayList<>(methods);
    Validate.isTrue(!copied.isEmpty(), "methods should not be empty.");
    for (int i = 0; i < copied.size(); i++) {
      if (copied.get(i) == null) {
        throw new NullPointerException("methods[" + i + ']');
      }
    }
    this.allowedRequestMethods.addAll(copied);
    return this;
  }

  /**
   * Specifies the headers that should be returned in the CORS {@code "Access-Control-Allow-Headers"}
   * response header.
   *
   * <p>If a client specifies headers on the request, for example by calling:
   * <pre>{@code
   * xhr.setRequestHeader('My-Custom-Header', 'SomeValue');
   * }</pre>
   * The server will receive the above header name in the {@code "Access-Control-Request-Headers"} of the
   * preflight request. The server will then decide if it allows this header to be sent for the
   * real request (remember that a preflight is not the real request but a request asking the server
   * if it allows a request).
   *
   * @param headers the headers to be added to
   *                the preflight {@code "Access-Control-Allow-Headers"} response header.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowRequestHeaders(CharSequence... headers) {
    Validate.notNull(headers, "headers");
    return allowRequestHeaders(new ArrayList<>(Arrays.asList(headers)));
  }

  /**
   * Specifies the headers that should be returned in the CORS {@code "Access-Control-Allow-Headers"}
   * response header.
   *
   * <p>If a client specifies headers on the request, for example by calling:
   * <pre>{@code
   * xhr.setRequestHeader('My-Custom-Header', 'SomeValue');
   * }</pre>
   * The server will receive the above header name in the {@code "Access-Control-Request-Headers"} of the
   * preflight request. The server will then decide if it allows this header to be sent for the
   * real request (remember that a preflight is not the real request but a request asking the server
   * if it allows a request).
   *
   * @param headers the headers to be added to
   *                the preflight {@code "Access-Control-Allow-Headers"} response header.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder allowRequestHeaders(List<? extends CharSequence> headers) {
    Validate.notNull(headers, "headers");
    final List<CharSequence> copied = new ArrayList<>(headers);
    Validate.isTrue(!copied.isEmpty(), "headers should not be empty.");
    for (int i = 0; i < copied.size(); i++) {
      if (copied.get(i) == null) {
        throw new NullPointerException("headers[" + i + ']');
      }
    }
    copied.stream().map(AsciiString::of).forEach(this.allowedRequestHeaders::add);
    return this;
  }

  /**
   * Specifies HTTP response headers that should be added to a CORS preflight response.
   *
   * <p>An intermediary like a load balancer might require that a CORS preflight request
   * have certain headers set. This enables such headers to be added.
   *
   * @param name the name of the HTTP header.
   * @param values the values for the HTTP header.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder preflightResponseHeader(CharSequence name, Object... values) {
    Validate.notNull(name, "name");
    Validate.notNull(values, "values");
    Validate.isTrue(values.length > 0, "values should not be empty.");
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null) {
        throw new NullPointerException("values[" + i + ']');
      }
    }
    this.preflightResponseHeaders.put(AsciiString.of(name), new ConstantValueSupplier(ArrayUtils.clone(values)));
    return this;
  }

  /**
   * Specifies HTTP response headers that should be added to a CORS preflight response.
   *
   * <p>An intermediary like a load balancer might require that a CORS preflight request
   * have certain headers set. This enables such headers to be added.
   *
   * @param name the name of the HTTP header.
   * @param values the values for the HTTP header.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder preflightResponseHeader(CharSequence name, List<?> values) {
    Validate.notNull(name, "name");
    Validate.notNull(values, "values");
    Validate.isTrue(!values.isEmpty(), "values should not be empty.");
    final List<Object> list = new ArrayList<>();
    int i = 0;
    for (Object value : values) {
      if (value == null) {
        throw new NullPointerException("value[" + i + ']');
      }
      list.add(value);
      i++;
    }
    this.preflightResponseHeaders.put(AsciiString.of(name), new ConstantValueSupplier(list));
    return this;
  }

  /**
   * Specifies HTTP response headers that should be added to a CORS preflight response.
   *
   * <p>An intermediary like a load balancer might require that a CORS preflight request
   * have certain headers set. This enables such headers to be added.
   *
   * <p>Some values must be dynamically created when the HTTP response is created, for
   * example the {@code "Date"} response header. This can be accomplished by using a {@link Supplier}
   * which will have its {@link Supplier#get()} method invoked when the HTTP response is created.
   *
   * @param name the name of the HTTP header.
   * @param valueSupplier a {@link Supplier} which will be invoked at HTTP response creation.
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder preflightResponseHeader(CharSequence name, Supplier<?> valueSupplier) {
    Validate.notNull(name, "name");
    Validate.notNull(valueSupplier, "valueSupplier");
    preflightResponseHeaders.put(AsciiString.of(name), valueSupplier);
    return this;
  }

  /**
   * Specifies that no preflight response headers should be added to a preflight response.
   *
   * @return {@code this} to support method chaining.
   */
  public AbstractCorsPolicyBuilder disablePreflightResponseHeaders() {
    this.preflightResponseHeadersDisabled = true;
    return this;
  }
}
