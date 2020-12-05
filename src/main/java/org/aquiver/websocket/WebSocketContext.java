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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.aquiver.RequestContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author WangYi
 * @since 2020/7/11
 */
public class WebSocketContext extends RequestContext {
  private final WebSocketSession session;
  private WebSocketChannel channel;
  private Throwable throwable;
  private String message;

  public WebSocketContext(FullHttpRequest httpRequest, ChannelHandlerContext context) {
    super(httpRequest, context);
    long id = ThreadLocalRandom.current().nextLong();
    this.session = new WebSocketSession(context, getId(id));
  }

  private String getId(long id) {
    return String.valueOf(id).replaceFirst("-", "");
  }

  public void message(String message) {
    this.session.channelHandlerContext()
            .writeAndFlush(new TextWebSocketFrame(message));
  }

  public void setChannel(WebSocketChannel channel) {
    this.channel = channel;
  }

  /**
   * Allows the user to disconnect the websocket
   */
  public void disconnect() {
    session.channelHandlerContext().disconnect().addListener(ChannelFutureListener.CLOSE);
  }

  public WebSocketSession session() {
    return session;
  }

  public WebSocketChannel channel() {
    return channel;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
