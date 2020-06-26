package org.aquiver.route.session;

import io.netty.channel.Channel;

import java.util.*;

/**
 * @author WangYi
 * @since 2020/6/19
 */
public final class HttpSession implements Session {
  private final Map<String, Object> attrs = new HashMap<>();
  private final List<String> attributeNames = new ArrayList<>();
  private final Channel channel;
  private volatile long lastAccessedTime;
  private final long creationTime;

  public HttpSession(Channel channel) {
    this.channel = channel;
    this.creationTime = System.currentTimeMillis();
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public String getId() {
    return channel.id().asLongText();
  }

  @Override
  public long getLastAccessedTime() {
    return Objects.isNull(lastAccessedTime) ? 0 : lastAccessedTime;
  }

  @Override
  public Object getAttribute(String key) {
    Objects.requireNonNull(key, "key must not be null");
    updateLastAccessedTime();
    return attrs.get(key);
  }

  @Override
  public List<String> getAttributeNames() {
    if (attrs.isEmpty()) {
      return new ArrayList<>();
    }
    this.attributeNames.addAll(attrs.keySet());
    this.updateLastAccessedTime();
    return attributeNames;
  }

  @Override
  public void setAttribute(String key, Object value) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(value, "value must not be null");
    this.attrs.put(key, value);
    this.updateLastAccessedTime();
  }

  @Override
  public void removeAttribute(String key) {
    Objects.requireNonNull(key, "key must not be null");
    this.attrs.remove(key);
    this.updateLastAccessedTime();
  }

  private synchronized void updateLastAccessedTime() {
    this.lastAccessedTime = System.currentTimeMillis();
  }
}
