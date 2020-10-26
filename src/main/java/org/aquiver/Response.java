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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author WangYi
 * @since 2020/10/19
 */
public interface Response {
  void cookie(String key, String value);

  void removeCookie(String key);

  void header(String key, String value);

  void removeHeader(String key);

  void tryPush(FullHttpResponse response);

  ChannelFuture tryPush(Object msg);

  ChannelFuture tryPush(Object msg, ChannelPromise promise);

  ChannelFuture tryWrite(Object msg);

  ChannelFuture tryWrite(Object msg, ChannelPromise promise);

  void redirect(String redirectUrl);

  <T> void json(T t);

  <T> void xml(T t);

  void text(String text);

  void render(String renderTemplate);

  void html(String htmlTemplate);

  void status(int httpStatus);

  void contentType();

  void contentType(String contentType);

  void notFound();

  void badRequest();

  void serverInternalError();
}
