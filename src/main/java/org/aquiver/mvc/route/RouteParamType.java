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
package org.aquiver.mvc.route;

public enum RouteParamType {

  /**
   * Request Url Param
   */
  REQUEST_PARAM,

  /**
   * Path variable
   */
  PATH_VARIABLE,

  /**
   * Http Request
   */
  HTTP_REQUEST,

  /**
   * Http Response
   */
  HTTP_RESPONSE,

  /**
   * 请求体
   */
  REQUEST_BODY,

  /**
   * X-WWW-FORM-URLENCODED
   */
  URL_ENCODED_FORM,

  /**
   * Cookies
   */
  REQUEST_COOKIES,

  /**
   * Http Request Header Param
   */
  REQUEST_HEADER,

  /**
   * Upload file
   */
  UPLOAD_FILE,

  /**
   * Multiple upload files
   */
  UPLOAD_FILES,

  /**
   * Request Session
   */
  REQUEST_SESSION,

  /**
   * File operations
   */
  MULTIPART_FILE,

  /**
   * Throwable
   */
  THROWABLE_CLASS
}
