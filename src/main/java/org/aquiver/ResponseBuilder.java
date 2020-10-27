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
package org.aquiver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.Validate;
import org.aquiver.mvc.http.HttpStatus;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public class ResponseBuilder {
  private static final ByteBuf EMPTY_BUF =
          Unpooled.copiedBuffer("", CharsetUtil.UTF_8);

  private final Set<Cookie> cookies = new HashSet<>();
  private final HttpHeaders httpHeaders = new DefaultHttpHeaders();
  private int httpStatus = HttpStatus.OK;
  private HttpVersion version = HttpVersion.HTTP_1_0;
  private ByteBuf byteBuf = EMPTY_BUF;

  public static ResponseBuilder builder() {
    return new ResponseBuilder();
  }

  public ResponseBuilder byteBuf(ByteBuf byteBuf) {
    Validate.notNull(byteBuf, "ByteBuf can't be null");
    this.byteBuf = byteBuf;
    return this;
  }

  public ResponseBuilder body(String body) {
    Validate.notNull(body, "Body can't be null");
    this.byteBuf = Unpooled.copiedBuffer(body, Charset.defaultCharset());
    return this;
  }

  public ResponseBuilder status(int httpStatus) {
    Validate.notNull(HttpResponseStatus.valueOf(httpStatus), "The http state is not supported");
    this.httpStatus = httpStatus;
    return this;
  }

  public ResponseBuilder version(HttpVersion version) {
    Validate.notNull(version, "HttpVersion can't be null");
    this.version = version;
    return this;
  }

  public ResponseBuilder header(CharSequence key, CharSequence value) {
    Validate.notNull(key, "HttpHeader key can't be null");
    Validate.notNull(value, "HttpHeader value can't be null");
    this.httpHeaders.add(key, value);
    return this;
  }

  public ResponseBuilder header(HttpHeaders httpHeaders) {
    Validate.notNull(httpHeaders, "HttpHeaders can't be null");
    this.httpHeaders.setAll(httpHeaders);
    return this;
  }

  public ResponseBuilder cookie(CharSequence key, CharSequence value) {
    Validate.notNull(key, "Cookie key can't be null");
    Validate.notNull(value, "cookie value can't be null");
    this.cookies.add(new DefaultCookie(key.toString(), value.toString()));
    return this;
  }

  public FullHttpResponse build() {
    final FullHttpResponse fullHttpResponse =
            new DefaultFullHttpResponse(version,
                    HttpResponseStatus.valueOf(httpStatus), byteBuf);

    if (!cookies.isEmpty()) {
      httpHeaders.set(HttpHeaderNames.COOKIE, cookies);
    }
    fullHttpResponse.headers().setAll(httpHeaders);
    return fullHttpResponse;
  }
}
