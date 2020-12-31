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
