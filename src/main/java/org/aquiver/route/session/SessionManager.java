package org.aquiver.route.session;

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

  public void addSession(Session session) {
    Objects.requireNonNull(session, "session can't be null");
    this.sessionPool.put(session.getId(), session);
  }

  public void clear() {
    this.sessionPool.clear();
  }
}
