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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/10/20
 */
public class Cookies {
  private static final Map<String, Cookie> cookies = new HashMap<>();

  protected static Map<String, Cookie> parse(HttpHeaders headers) {
    String cookie = headers.get("Cookie");
    if(StringUtils.isNotBlank(cookie)){
      ServerCookieDecoder.LAX.decode(cookie).forEach(Cookies::parseCookie);
    }
    return cookies;
  }

  /**
   * Parse netty cookie to {@link Cookie}.
   *
   * @param nettyCookie netty raw cookie instance
   */
  private static void parseCookie(io.netty.handler.codec.http.cookie.Cookie nettyCookie) {
    Cookie cookie = new Cookie(nettyCookie.name(), nettyCookie.value());
    cookie.setDomain(nettyCookie.domain());
    cookie.setExpires(LocalDateTime.now());
    cookie.setHttpOnly(nettyCookie.isHttpOnly());
    cookie.setMaxAge(nettyCookie.maxAge());
    cookie.setPath(nettyCookie.path());
    cookie.setSecure(nettyCookie.isSecure());
    Cookies.cookies.put(cookie.getName(), cookie);
  }
}
