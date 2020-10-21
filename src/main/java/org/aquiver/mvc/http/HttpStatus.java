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
package org.aquiver.mvc.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public interface HttpStatus {
  int CONTINUE = HttpResponseStatus.CONTINUE.code();
  int SWITCHING_PROTOCOLS = HttpResponseStatus.SWITCHING_PROTOCOLS.code();
  int OK = HttpResponseStatus.OK.code();
  int CREATED = HttpResponseStatus.CREATED.code();
  int ACCEPTED = HttpResponseStatus.ACCEPTED.code();
  int NON_AUTHORITATIVE_INFORMATION = HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION.code();
  int NO_CONTENT = HttpResponseStatus.NO_CONTENT.code();
  int RESET_CONTENT = HttpResponseStatus.RESET_CONTENT.code();
  int PARTIAL_CONTENT = HttpResponseStatus.PARTIAL_CONTENT.code();
  int MULTIPLE_CHOICES = HttpResponseStatus.MULTIPLE_CHOICES.code();
  int MOVED_PERMANENTLY = HttpResponseStatus.MOVED_PERMANENTLY.code();
  int MOVED_TEMPORARILY = HttpResponseStatus.FOUND.code();
  int FOUND = HttpResponseStatus.FOUND.code();
  int SEE_OTHER = HttpResponseStatus.SEE_OTHER.code();
  int NOT_MODIFIED = HttpResponseStatus.NOT_MODIFIED.code();
  int USE_PROXY = HttpResponseStatus.USE_PROXY.code();
  int TEMPORARY_REDIRECT = HttpResponseStatus.TEMPORARY_REDIRECT.code();
  int BAD_REQUEST = HttpResponseStatus.BAD_REQUEST.code();
  int UNAUTHORIZED = HttpResponseStatus.UNAUTHORIZED.code();
  int PAYMENT_REQUIRED = HttpResponseStatus.PAYMENT_REQUIRED.code();
  int FORBIDDEN = HttpResponseStatus.FORBIDDEN.code();
  int NOT_FOUND = HttpResponseStatus.NOT_FOUND.code();
  int METHOD_NOT_ALLOWED = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
  int NOT_ACCEPTABLE = HttpResponseStatus.NOT_ACCEPTABLE.code();
  int PROXY_AUTHENTICATION_REQUIRED = HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED.code();
  int REQUEST_TIMEOUT = HttpResponseStatus.REQUEST_TIMEOUT.code();
  int CONFLICT = HttpResponseStatus.CONFLICT.code();
  int GONE = HttpResponseStatus.GONE.code();
  int LENGTH_REQUIRED = HttpResponseStatus.LENGTH_REQUIRED.code();
  int PRECONDITION_FAILED = HttpResponseStatus.PRECONDITION_FAILED.code();
  int REQUEST_ENTITY_TOO_LARGE = HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code();
  int REQUEST_URI_TOO_LONG = HttpResponseStatus.REQUEST_URI_TOO_LONG.code();
  int UNSUPPORTED_MEDIA_TYPE = HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.code();
  int REQUESTED_RANGE_NOT_SATISFIABLE = HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE.code();
  int EXPECTATION_FAILED = HttpResponseStatus.EXPECTATION_FAILED.code();
  int INTERNAL_SERVER_ERROR = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
  int NOT_IMPLEMENTED = HttpResponseStatus.NOT_IMPLEMENTED.code();
  int BAD_GATEWAY = HttpResponseStatus.BAD_GATEWAY.code();
  int SERVICE_UNAVAILABLE = HttpResponseStatus.SERVICE_UNAVAILABLE.code();
  int GATEWAY_TIMEOUT = HttpResponseStatus.GATEWAY_TIMEOUT.code();
  int HTTP_VERSION_NOT_SUPPORTED = HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED.code();
  int VARIANT_ALSO_NEGOTIATES = HttpResponseStatus.VARIANT_ALSO_NEGOTIATES.code();
  int INSUFFICIENT_STORAGE = HttpResponseStatus.INSUFFICIENT_STORAGE.code();
  int NOT_EXTENDED = HttpResponseStatus.NOT_EXTENDED.code();
  int NETWORK_AUTHENTICATION_REQUIRED = HttpResponseStatus.NETWORK_AUTHENTICATION_REQUIRED.code();
}
