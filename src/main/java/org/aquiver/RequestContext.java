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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.commons.lang3.Validate;
import org.aquiver.mvc.http.Cookie;
import org.aquiver.mvc.http.Header;
import org.aquiver.mvc.http.HttpRequest;
import org.aquiver.mvc.http.HttpResponse;
import org.aquiver.mvc.router.RouteInfo;
import org.aquiver.mvc.router.session.Session;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/27
 */
public class RequestContext implements Request, Response, RequestChannel {
  private final static ThreadLocal<RequestContext> threadContext = new InheritableThreadLocal<>();
  private final HttpRequest httpRequest;
  private final HttpResponse response;
  private final ChannelHandlerContext context;
  private Throwable throwable;
  private RouteInfo routeInfo;

  public RequestContext(FullHttpRequest httpRequest, ChannelHandlerContext context) {
    Validate.notNull(httpRequest, "FullHttpRequest can't be null");
    Validate.notNull(context, "ChannelHandlerContext can't be null");

    this.context = context;
    this.httpRequest = HttpRequest.of(httpRequest, this.context);
    this.response = HttpResponse.of(this.context);
  }

  public static RequestContext current() {
    return threadContext.get();
  }

  public void routeInfo(RouteInfo routeInfo) {
    Validate.notNull(routeInfo, "RouteInfo can't be null");
    this.routeInfo = requireNonNull(routeInfo);
  }

  public void throwable(Throwable throwable) {
    Validate.notNull(throwable, "throwable can't be null");
    this.throwable = requireNonNull(throwable);
  }

  private Channel getChannel() {
    return this.context.channel();
  }

  public RouteInfo routeInfo() {
    return routeInfo;
  }

  public Request request() {
    return httpRequest;
  }

  public Response response() {
    return response;
  }

  public Throwable throwable() {
    return throwable;
  }

  @Override
  public ChannelHandlerContext channelContext() {
    return this.context;
  }

  @Override
  public void closeChannel() {
    this.getChannel().close();
  }

  @Override
  public boolean isOpen() {
    return this.getChannel().isOpen();
  }

  @Override
  public boolean isRegistered() {
    return this.getChannel().isRegistered();
  }

  @Override
  public boolean isActive() {
    return this.getChannel().isActive();
  }

  @Override
  public boolean isWritable() {
    return this.getChannel().isWritable();
  }

  @Override
  public long bytesBeforeUnwritable() {
    return this.getChannel().bytesBeforeUnwritable();
  }

  @Override
  public long bytesBeforeWritable() {
    return this.getChannel().bytesBeforeWritable();
  }

  @Override
  public void tryPush(FullHttpResponse response) {
    this.response().tryPush(response);
  }

  @Override
  public ChannelFuture tryWrite(Object object) {
    return this.response().tryWrite(object);
  }

  @Override
  public void tryPush(Object msg) {
    this.response().tryPush(msg);
  }

  @Override
  public void redirect(String redirectUrl) {
    this.response().redirect(redirectUrl);
  }

  @Override
  public <T> void json(T t) {
    this.response().json(t);
  }

  @Override
  public <T> void xml(T t) {
    this.response().xml(t);
  }

  @Override
  public void text(String text) {
    this.response().text(text);
  }

  @Override
  public void render(String renderTemplate) {
    this.response().render(renderTemplate);
  }

  @Override
  public void html(String htmlTemplate) {
    this.response().html(htmlTemplate);
  }

  @Override
  public void status(int httpStatus) {
    this.response().status(httpStatus);
  }

  @Override
  public void contentType() {
    this.response().contentType();
  }

  @Override
  public void contentType(String contentType) {
    this.response().contentType(contentType);
  }

  @Override
  public void notFound() {
    this.response().notFound();
  }

  @Override
  public void badRequest(){
    this.response().badRequest();
  }

  @Override
  public void serverInternalError() {
    this.response().serverInternalError();
  }

  @Override
  public Boolean isMultipart() {
    return this.request().isMultipart();
  }

  @Override
  public ByteBuf body() {
    return this.request().body();
  }

  @Override
  public Session session() {
    return this.request().session();
  }

  @Override
  public String method() {
    return this.request().method();
  }

  @Override
  public String protocol() {
    return this.request().protocol();
  }

  @Override
  public Integer minorVersion() {
    return this.request().minorVersion();
  }

  @Override
  public Integer majorVersion() {
    return this.request().majorVersion();
  }

  @Override
  public String header(String key) {
    return this.request().header(key);
  }

  @Override
  public String param(String key) {
    return this.request().param(key);
  }

  @Override
  public Set<String> paramNames() {
    return this.request().paramNames();
  }

  @Override
  public Set<String> headerNames() {
    return this.request().headerNames();
  }

  @Override
  public String uri() {
    return this.request().uri();
  }

  @Override
  public String path() {
    return this.request().path();
  }

  @Override
  public String rawPath() {
    return this.request().rawPath();
  }

  @Override
  public String rawQuery() {
    return this.request().rawQuery();
  }

  @Override
  public Integer port() {
    return this.request().port();
  }

  @Override
  public String remoteAddress() {
    return this.request().remoteAddress();
  }

  @Override
  public String host() {
    return this.request().host();
  }

  @Override
  public String accept() {
    return this.request().accept();
  }

  @Override
  public String connection() {
    return this.request().connection();
  }

  @Override
  public Map<String, Cookie> cookies() {
    return this.request().cookies();
  }

  @Override
  public Cookie cookie(String key) {
    return this.request().cookie(key);
  }

  @Override
  public String sessionKey() {
    return this.request().sessionKey();
  }

  @Override
  public String referer() {
    return this.request().referer();
  }

  @Override
  public String userAgent() {
    return this.request().userAgent();
  }

  @Override
  public Map<String, Header> header() {
    return this.request().header();
  }

  @Override
  public Boolean isKeepAlive() {
    return this.request().isKeepAlive();
  }

  @Override
  public Boolean is100ContinueExpected() {
    return this.request().is100ContinueExpected();
  }
}
