package org.aquiver.route.session;

import org.aquiver.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

  public void addSession(Request request) {
    Objects.requireNonNull(request, "request can't be null");
    Session httpSession = getSession(request);
    if (Objects.isNull(httpSession)) {
      httpSession = new HttpSession(request.channelHandlerContext().channel());
      sessionPool.put(httpSession.getId(), httpSession);
    }
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
