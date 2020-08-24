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
package org.aquiver.result;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public final class HttpStatus {
  public static final HttpStatus CONTINUE = new HttpStatus(HttpResponseStatus.CONTINUE.code());
  public static final HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(HttpResponseStatus.SWITCHING_PROTOCOLS.code());
  public static final HttpStatus OK = new HttpStatus(HttpResponseStatus.OK.code());
  public static final HttpStatus CREATED = new HttpStatus(HttpResponseStatus.CREATED.code());
  public static final HttpStatus ACCEPTED = new HttpStatus(HttpResponseStatus.ACCEPTED.code());
  public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = new HttpStatus(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION.code());
  public static final HttpStatus NO_CONTENT = new HttpStatus(HttpResponseStatus.NO_CONTENT.code());
  public static final HttpStatus RESET_CONTENT = new HttpStatus(HttpResponseStatus.RESET_CONTENT.code());
  public static final HttpStatus PARTIAL_CONTENT = new HttpStatus(HttpResponseStatus.PARTIAL_CONTENT.code());
  public static final HttpStatus MULTIPLE_CHOICES = new HttpStatus(HttpResponseStatus.MULTIPLE_CHOICES.code());
  public static final HttpStatus MOVED_PERMANENTLY = new HttpStatus(HttpResponseStatus.MOVED_PERMANENTLY.code());
  public static final HttpStatus MOVED_TEMPORARILY = new HttpStatus(HttpResponseStatus.FOUND.code());
  public static final HttpStatus FOUND = new HttpStatus(HttpResponseStatus.FOUND.code());
  public static final HttpStatus SEE_OTHER = new HttpStatus(HttpResponseStatus.SEE_OTHER.code());
  public static final HttpStatus NOT_MODIFIED = new HttpStatus(HttpResponseStatus.NOT_MODIFIED.code());
  public static final HttpStatus USE_PROXY = new HttpStatus(HttpResponseStatus.USE_PROXY.code());
  public static final HttpStatus TEMPORARY_REDIRECT = new HttpStatus(HttpResponseStatus.TEMPORARY_REDIRECT.code());
  public static final HttpStatus BAD_REQUEST = new HttpStatus(HttpResponseStatus.BAD_REQUEST.code());
  public static final HttpStatus UNAUTHORIZED = new HttpStatus(HttpResponseStatus.UNAUTHORIZED.code());
  public static final HttpStatus PAYMENT_REQUIRED = new HttpStatus(HttpResponseStatus.PAYMENT_REQUIRED.code());
  public static final HttpStatus FORBIDDEN = new HttpStatus(HttpResponseStatus.FORBIDDEN.code());
  public static final HttpStatus NOT_FOUND = new HttpStatus(HttpResponseStatus.NOT_FOUND.code());
  public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(HttpResponseStatus.METHOD_NOT_ALLOWED.code());
  public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(HttpResponseStatus.NOT_ACCEPTABLE.code());
  public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED.code());
  public static final HttpStatus REQUEST_TIMEOUT = new HttpStatus(HttpResponseStatus.REQUEST_TIMEOUT.code());
  public static final HttpStatus CONFLICT = new HttpStatus(HttpResponseStatus.CONFLICT.code());
  public static final HttpStatus GONE = new HttpStatus(HttpResponseStatus.GONE.code());
  public static final HttpStatus LENGTH_REQUIRED = new HttpStatus(HttpResponseStatus.LENGTH_REQUIRED.code());
  public static final HttpStatus PRECONDITION_FAILED = new HttpStatus(HttpResponseStatus.PRECONDITION_FAILED.code());
  public static final HttpStatus REQUEST_ENTITY_TOO_LARGE = new HttpStatus(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code());
  public static final HttpStatus REQUEST_URI_TOO_LONG = new HttpStatus(HttpResponseStatus.REQUEST_URI_TOO_LONG.code());
  public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = new HttpStatus(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.code());
  public static final HttpStatus REQUESTED_RANGE_NOT_SATISFIABLE = new HttpStatus(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE.code());
  public static final HttpStatus EXPECTATION_FAILED = new HttpStatus(HttpResponseStatus.EXPECTATION_FAILED.code());
  public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(HttpResponseStatus.NOT_IMPLEMENTED.code());
  public static final HttpStatus BAD_GATEWAY = new HttpStatus(HttpResponseStatus.BAD_GATEWAY.code());
  public static final HttpStatus SERVICE_UNAVAILABLE = new HttpStatus(HttpResponseStatus.SERVICE_UNAVAILABLE.code());
  public static final HttpStatus GATEWAY_TIMEOUT = new HttpStatus(HttpResponseStatus.GATEWAY_TIMEOUT.code());
  public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED.code());
  private final int status;
  private final String message;

  public HttpStatus(int status) {
    this.status = status;
    this.message = null;
  }

  public HttpStatus(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
