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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.aquiver.RequestContext;

/**
 * @author WangYi
 * @since 2020/7/11
 */
public class WebSocketContext {
  private final RequestContext requestContext;
  private final WebSocketSession session;
  private final WebSocketChannel channel;
  private Message message;
  private Error error;

  private WebSocketContext(RequestContext requestContext, WebSocketSession session, WebSocketChannel channel) {
    this.requestContext = requestContext;
    this.session = session;
    this.channel = channel;
  }

  public static WebSocketContext create(RequestContext requestContext,
                                        WebSocketSession webSocketSession,
                                        WebSocketChannel webSocketChannel) {
    return new WebSocketContext(requestContext, webSocketSession, webSocketChannel);
  }

  public void message(String message) {
    this.session.channelHandlerContext()
            .writeAndFlush(new TextWebSocketFrame(message));
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

  public RequestContext requestContext() {
    return requestContext;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public Error getError() {
    return error;
  }

  public void setError(Error error) {
    this.error = error;
  }

  public static class Message {
    private final String text;
    private final WebSocketContext context;

    Message(String text, WebSocketContext context) {
      this.text = text;
      this.context = context;
    }

    public String text() {
      return text;
    }

    public WebSocketContext context() {
      return context;
    }

    @Override
    public String toString() {
      return "Message{" +
              "text='" + text + '\'' +
              '}';
    }
  }

  public static class Error {
    private final Throwable cause;
    private final WebSocketContext context;

    Error(Throwable cause, WebSocketContext context) {
      this.cause = cause;
      this.context = context;
    }

    public Throwable cause() {
      return cause;
    }

    public WebSocketContext context() {
      return context;
    }

    @Override
    public String toString() {
      return "Error{" +
              "cause=" + cause +
              '}';
    }
  }
}
