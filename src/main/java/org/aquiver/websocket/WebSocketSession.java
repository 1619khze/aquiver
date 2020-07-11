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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.aquiver.mvc.route.session.Session;

import java.util.*;

/**
 * @author WangYi
 * @since 2020/7/11
 */
public class WebSocketSession implements Session {
  private final Map<String, Object> attrs = new HashMap<>();
  private final List<String> attributeNames = new ArrayList<>();
  private final Channel channel;
  private final long creationTime;
  private final String id;
  private volatile long lastAccessedTime;
  private final ChannelHandlerContext channelHandlerContext;

  WebSocketSession(ChannelHandlerContext channelHandlerContext, String id) {
    this.channelHandlerContext = channelHandlerContext;
    this.channel = channelHandlerContext.channel();
    this.id = id;
    this.creationTime = System.currentTimeMillis();
  }

  @Override
  public ChannelHandlerContext channelHandlerContext() {
    return channelHandlerContext;
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getIp() {
    return channel.remoteAddress().toString();
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
