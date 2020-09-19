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
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.nio.charset.StandardCharsets;

import static org.apex.Const.BLANK;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public class ResultResponseBuilder {
  private HttpResponseStatus status;
  private HttpVersion version;
  private Cookie cookie;
  private HttpHeaders headers;
  private ByteBuf byteBuf;

  public static ResultResponseBuilder forResponse() {
    return forResponse(BLANK);
  }

  public static ResultResponseBuilder forResponse(Object value) {
    return forResponse(String.valueOf(value));
  }

  public static ResultResponseBuilder forResponse(HttpResponseStatus status) {
    return forResponse(status, BLANK);
  }

  public static ResultResponseBuilder forResponse(HttpResponseStatus status, String value) {
    return forResponse(HttpVersion.HTTP_1_1, status, Unpooled
            .copiedBuffer(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static ResultResponseBuilder forResponse(HttpVersion version, String value) {
    return forResponse(version, HttpResponseStatus.OK, Unpooled
            .copiedBuffer(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static ResultResponseBuilder forResponse(String value) {
    return forResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled
            .copiedBuffer(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static ResultResponseBuilder forResponse(ByteBuf byteBuf) {
    return forResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
  }

  public static ResultResponseBuilder forResponse(HttpHeaders headers, String value) {
    return forResponse(headers, Unpooled.copiedBuffer(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static ResultResponseBuilder forResponse(HttpHeaders headers, Object value) {
    return forResponse(headers, Unpooled.copiedBuffer(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
  }

  public static ResultResponseBuilder forResponse(HttpHeaders headers, ByteBuf byteBuf) {
    return forResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf, headers);
  }

  public static ResultResponseBuilder forResponse(HttpVersion version) {
    return new ResultResponseBuilder().version(version);
  }

  public static ResultResponseBuilder forResponse(HttpVersion version, HttpResponseStatus status) {
    return new ResultResponseBuilder().version(version).status(status);
  }

  public static ResultResponseBuilder forResponse(HttpVersion version, HttpResponseStatus status, ByteBuf byteBuf) {
    return new ResultResponseBuilder().version(version).status(status).byteBuf(byteBuf);
  }

  public static ResultResponseBuilder forResponse(HttpVersion version, HttpResponseStatus status, ByteBuf byteBuf,
                                                  HttpHeaders headers) {
    return new ResultResponseBuilder().version(version).status(status).byteBuf(byteBuf).headers(headers);
  }

  public static ResultResponseBuilder forResponse(HttpVersion version, HttpResponseStatus status, ByteBuf byteBuf,
                                                  HttpHeaders headers, Cookie cookie) {
    return new ResultResponseBuilder().version(version).status(status).byteBuf(byteBuf).headers(headers).cookies(cookie);
  }

  public ResultResponseBuilder byteBuf(ByteBuf byteBuf) {
    this.byteBuf = byteBuf;
    return this;
  }

  public ResultResponseBuilder status(HttpResponseStatus status) {
    this.status = status;
    return this;
  }

  public ResultResponseBuilder version(HttpVersion version) {
    this.version = version;
    return this;
  }

  public ResultResponseBuilder headers(HttpHeaders headers) {
    this.headers = headers;
    return this;
  }

  public ResultResponseBuilder cookies(Cookie cookie) {
    this.cookie = cookie;
    return this;
  }

  public FullHttpResponse build() {
    this.status = this.status != null ? status : HttpResponseStatus.OK;
    this.version = this.version != null ? version : HttpVersion.HTTP_1_1;
    this.byteBuf = this.byteBuf != null ? byteBuf : new EmptyByteBuf(
            new UnpooledByteBufAllocator(true));

    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(version, status, byteBuf);
    HttpHeaders headers = fullHttpResponse.headers();
    this.setDefaultHeader(fullHttpResponse, headers);

    if (this.headers != null) {
      headers.setAll(this.headers);
    }
    if (this.cookie != null) {
      headers.set(HttpHeaderNames.SET_COOKIE, cookie);
    }
    return fullHttpResponse;
  }

  private void setDefaultHeader(FullHttpResponse fullHttpResponse, HttpHeaders headers) {
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with,content-type");
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "POST,GET");
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    headers.set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
  }
}
