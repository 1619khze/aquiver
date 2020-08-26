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
package org.aquiver.mvc.router.session;

import org.aquiver.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author WangYi
 * @since 2020/6/19
 */
public final class SessionManager {
  private final Map<String, Session> sessionPool = new HashMap<>();

  public Session session(String sessionId) {
    Objects.requireNonNull(sessionId, "sessionId can't be null");
    return this.sessionPool.get(sessionId);
  }

  public Session createSession(Request request) {
    Objects.requireNonNull(request, "request can't be null");
    Session httpSession = getSession(request);
    if (Objects.isNull(httpSession)) {
      long l = ThreadLocalRandom.current().nextLong();
      httpSession = new HttpSession(request.channelHandlerContext(),String.valueOf(l)
              .replaceFirst("-",""));
      this.sessionPool.put(httpSession.getId(), httpSession);
    }

    return httpSession;
  }

  public void clear() {
    this.sessionPool.clear();
  }

  private Session getSession(Request request) {
    Object cookieHeader = request.cookies(request.sessionKey());
    if (Objects.isNull(cookieHeader)) {
      return null;
    }
    return session(String.valueOf(cookieHeader));
  }
}
