package org.aquiver.loader;

import org.aquiver.Aquiver;
import org.aquiver.utils.ReflectionUtils;
import org.aquiver.websocket.WebSocket;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/12
 */
public class WebSocketLoader implements WebLoader {
  /**
   * Filter out the classes marked with Web Socket annotations
   * from the scan result set and reduce them to the Web Socket
   * list of the routing manager
   *
   * @param instances Scanned result
   * @throws ReflectiveOperationException Exception superclass when
   *                                      performing reflection operation
   */
  @Override
  public void load(Map<String, Object> instances, Aquiver aquiver) throws Exception {
    for (Object object : instances.values()) {
      Class<?> cls = object.getClass();
      Method[] declaredMethods = cls.getDeclaredMethods();
      if (!ReflectionUtils.isNormal(cls)
              || !cls.isAnnotationPresent(WebSocket.class)
              || declaredMethods.length == 0) continue;
      aquiver.routeManager().addWebSocket(cls);
    }
  }
}
