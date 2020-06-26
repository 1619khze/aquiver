package org.aquiver.route.session;

import java.util.List;

/**
 * @author WangYi
 * @since 2020/6/19
 */
public interface Session {
  long getCreationTime();

  String getId();

  long getLastAccessedTime();

  Object getAttribute(String key);

  List<String> getAttributeNames();

  void setAttribute(String key, Object value);

  void removeAttribute(String key);
}
