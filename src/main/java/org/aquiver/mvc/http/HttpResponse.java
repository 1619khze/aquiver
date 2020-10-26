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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.apache.commons.lang3.Validate;
import org.aquiver.Response;
import org.aquiver.ResultResponseBuilder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    this.context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
    final HttpHeaders headers = new DefaultHttpHeaders();
    headers.set(HttpHeaderNames.LOCATION, redirectUrl);
    FullHttpResponse response = ResultResponseBuilder
            .forResponse(HttpResponseStatus.PERMANENT_REDIRECT)
            .headers(headers)
            .build();
    this.tryPush(response);
  }

  @Override
  public <T> void json(T t) {
    final HttpHeaders headers = new DefaultHttpHeaders();
    headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    String json = JSONObject.toJSONString(t);
    ByteBuf byteBuf = Unpooled.copiedBuffer(json, Charset.defaultCharset());
    FullHttpResponse response = ResultResponseBuilder
            .forResponse(HttpResponseStatus.OK)
            .headers(headers)
            .byteBuf(byteBuf)
            .build();
    this.tryPush(response);
  }

  @Override
  public <T> void xml(T t) {

  }

  @Override
  public void text(String text) {

  }

  @Override
  public void render(String renderTemplate) {

  }

  @Override
  public void html(String htmlTemplate) {

  }

  @Override
  public void status(int httpStatus) {

  }

  @Override
  public void contentType() {

  }

  @Override
  public void contentType(String contentType) {

  }

  @Override
  public void notFound() {

  }

  @Override
  public void badRequest() {

  }

  @Override
  public void serverInternalError() {

  }
}
