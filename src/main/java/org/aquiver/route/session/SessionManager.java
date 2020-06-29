package org.aquiver.route.session;

import io.netty.channel.Channel;
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
      Channel channel = request.channelHandlerContext().channel();
      long l = ThreadLocalRandom.current().nextLong();
      httpSession = new HttpSession(channel,String.valueOf(l)
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
