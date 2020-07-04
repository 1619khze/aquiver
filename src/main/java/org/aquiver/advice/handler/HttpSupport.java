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
package org.aquiver.advice.handler;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author WangYi
 * @since 2020/5/29
 */
interface HttpSupport {
  /**
   * Returns the {@link HttpResponseStatus} represented by the specified code.
   * If the specified code is a standard HTTP status code, a cached instance
   * will be returned.  Otherwise, a new instance will be returned.
   */
  boolean support(int code);

  /**
   * Returns the {@link HttpResponseStatus} represented by the specified {@code code} and {@code reasonPhrase}.
   * If the specified code is a standard HTTP status {@code code} and {@code reasonPhrase}, a cached instance
   * will be returned. Otherwise, a new instance will be returned.
   *
   * @param code         The response code value.
   * @param reasonPhrase The response code reason phrase.
   * @return the {@link HttpResponseStatus} represented by the specified {@code code} and {@code reasonPhrase}.
   */
  boolean support(int code, String reasonPhrase);
}
