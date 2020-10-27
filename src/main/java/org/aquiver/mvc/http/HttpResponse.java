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

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.apache.commons.lang3.Validate;
import org.aquiver.Response;
import org.aquiver.ResponseBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.PERMANENT_REDIRECT;
import static io.netty.util.CharsetUtil.UTF_8;

import static org.aquiver.mvc.http.MediaType.APPLICATION_JSON_VALUE;
import static org.aquiver.mvc.http.MediaType.TEXT_HTML_VALUE;
import static org.aquiver.mvc.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Response class based on Netty-based Full Http Response
 *
 * @author WangYi
 * @since 2020/6/27
 */
public class HttpResponse implements Response {
  private final Map<String, String> headers = new HashMap<>();
  private final Set<io.netty.handler.codec.http.cookie.Cookie> cookies = new HashSet<>();
  private final ChannelHandlerContext context;

  private HttpResponse(ChannelHandlerContext context) {
    Validate.notNull(context, "ChannelHandlerContext can't be null");
    this.context = context;
  }

  public static HttpResponse of(ChannelHandlerContext context) {
    Validate.notNull(context, "ChannelHandlerContext can't be null");
    return new HttpResponse(context);
  }

  @Override
  public void cookie(String key, String value) {
    Validate.notNull(key, "Cookie key can't be null");
    Validate.notNull(value, "Cookie value can't be null");
    this.cookies.add(new DefaultCookie(key, value));
  }

  @Override
  public void removeCookie(String key) {
    Validate.notNull(key, "Cookie key can't be null");
    this.headers.remove(key);
  }

  @Override
  public void header(String key, String value) {
    Validate.notNull(key, "Header key can't be null");
    Validate.notNull(value, "Header value can't be null");
    this.headers.put(key, value);
  }

  @Override
  public void removeHeader(String key) {
    Validate.notNull(key, "Header key can't be null");
    this.headers.remove(key);
  }

  @Override
  public void tryPush(FullHttpResponse response) {
    this.context.writeAndFlush(response.retain()).addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public ChannelFuture tryPush(Object msg) {
    return this.context.writeAndFlush(msg);
  }

  @Override
  public ChannelFuture tryWrite(Object msg) {
    return this.context.write(msg);
  }

  @Override
  public ChannelFuture tryWrite(Object msg, ChannelPromise promise) {
    return this.context.write(msg, promise);
  }

  @Override
  public ChannelFuture tryPush(Object msg, ChannelPromise promise) {
    return this.context.writeAndFlush(msg, promise);
  }

  @Override
  public void redirect(String redirectUrl) {
    this.tryPush(ResponseBuilder.builder()
            .header(LOCATION, redirectUrl)
            .status(PERMANENT_REDIRECT.code())
            .build());
  }

  @Override
  public <T> void json(T t) {
    Validate.notNull(t, "JSON Object can't be null");
    final String json = JSONObject.toJSONString(t);
    final ByteBuf byteBuf = Unpooled.copiedBuffer(json, UTF_8);
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .status(HttpStatus.OK)
            .byteBuf(byteBuf)
            .build());
  }

  @Override
  public void text(String text) {
    Validate.notNull(text, "Text can't be null");
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, TEXT_PLAIN_VALUE)
            .status(HttpStatus.OK)
            .body(text)
            .build());
  }

  @Override
  public void render(String renderTemplate) {
    Validate.notNull(renderTemplate, "Template can't be null");
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, TEXT_PLAIN_VALUE)
            .status(HttpStatus.OK)
            .body(renderTemplate)
            .build());
  }

  @Override
  public void html(String htmlTemplate) {
    Validate.notNull(htmlTemplate, "Html can't be null");
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, TEXT_HTML_VALUE)
            .status(HttpStatus.OK)
            .body(htmlTemplate)
            .build());
  }

  @Override
  public void status(int httpStatus) {
    Validate.notNull(HttpResponseStatus.valueOf(httpStatus), "Http status not support");
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, TEXT_PLAIN_VALUE)
            .status(httpStatus)
            .build());
  }

  @Override
  public void notFound() {
    this.error(HttpStatus.NOT_FOUND, HttpResponseStatus.NOT_FOUND.reasonPhrase());
  }

  @Override
  public void badRequest() {
    this.error(HttpStatus.BAD_REQUEST, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
  }

  @Override
  public void serverInternalError() {
    this.error(HttpStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
  }

  @Override
  public void error(int httpStatus, String errorMsg) {
    Validate.notNull(HttpResponseStatus.valueOf(httpStatus), "Http status not support");
    Validate.notNull(errorMsg, "Error msg can't be null");
    this.tryPush(ResponseBuilder.builder()
            .header(CONTENT_TYPE, TEXT_PLAIN_VALUE)
            .status(httpStatus)
            .body(errorMsg)
            .build());
  }
}
