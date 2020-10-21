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
import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.mvc.http.Cookie;
import org.aquiver.mvc.http.Header;
import org.aquiver.mvc.router.session.Session;

import java.util.Map;
import java.util.Set;

/**
 * @author WangYi
 * @since 2020/10/17
 */
public interface Request {
  Map<String, FileUpload> fileUploads();

  FileUpload fileUploads(String fileKey);

  Boolean isMultipart();

  ByteBuf body();

  <T> T body(Class<T> type);

  Session session();

  String method();

  String protocol();

  Integer minorVersion();

  Integer majorVersion();

  String header(String key);

  String param(String key);

  Set<String> paramNames();

  Set<String> headerNames();

  String uri();

  String path();

  String rawPath();

  String rawQuery();

  Integer port();

  String remoteAddress();

  String host();

  String accept();

  String connection();

  Map<String, Cookie> cookies();

  Cookie cookie(String key);

  String sessionKey();

  String referer();

  String userAgent();

  Map<String, Header> header();

  Boolean isKeepAlive();

  Boolean is100ContinueExpected();
}
