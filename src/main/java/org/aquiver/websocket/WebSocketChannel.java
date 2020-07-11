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

/**
 * @author WangYi
 * @since 2020/7/11
 */
public interface WebSocketChannel {
  void onConnect(WebSocketContext webSocketContext);

  void onMessage(final Message message);

  void onClose(final WebSocketSession session);

  void onError(final Error error);

  class Message {
    public String text;
    public WebSocketSession session;

    Message(final String text, final WebSocketSession session) {
      this.text = text;
      this.session = session;
    }
  }

  class Error {
    public Throwable cause;
    public WebSocketSession session;

    Error(final Throwable cause, final WebSocketSession session) {
      this.cause = cause;
      this.session = session;
    }
  }
}
