package org.aquiver.websocket;

import org.apex.ApexContext;
import org.aquiver.RouteRepeatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/8/28
 */
public class WebSocketResolver {
  private final Map<String, WebSocketChannel> webSockets = new ConcurrentHashMap<>(4);
  private final ApexContext context = ApexContext.of();

  public void registerWebSocket(String path, WebSocketChannel webSocketChannel) {
    this.webSockets.put(path, webSocketChannel);
  }

  public void registerWebSocket(Class<?> websocketChannel) {
    List<Class<?>> implInterface = new ArrayList<>(Arrays.asList(websocketChannel.getInterfaces()));
    if (!websocketChannel.isAnnotationPresent(WebSocket.class)) {
      return;
    }

    WebSocket webSocket = websocketChannel.getAnnotation(WebSocket.class);
    String webSocketPath = webSocket.value();
    if (webSocketPath.equals("")) {
      webSocketPath = "/";
    }
    if (this.webSockets.containsKey(webSocketPath)) {
      throw new RouteRepeatException("Registered websocket channel URL is duplicated : " + webSocketPath);
    } else {
      if (implInterface.contains(WebSocketChannel.class)) {
        final WebSocketChannel channel = (WebSocketChannel) context.addBean(websocketChannel);
        webSockets.put(webSocketPath, channel);
      } else {
        final WebSocketWrapper wrapper = new WebSocketWrapper();
        wrapper.initialize(websocketChannel);
        this.webSockets.put(webSocketPath, wrapper);
      }
    }
  }

  public WebSocketChannel lookup(String path) {
    return this.webSockets.getOrDefault(path, null);
  }
}
