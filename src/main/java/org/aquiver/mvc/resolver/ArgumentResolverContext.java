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
package org.aquiver.mvc.resolver;

import org.aquiver.RequestContext;
import org.aquiver.websocket.WebSocketContext;

/**
 * @author WangYi
 * @since 2020/7/20
 */
public class ArgumentResolverContext {
  private RequestContext requestContext;
  private WebSocketContext webSocketContext;
  private Throwable throwable;

  public void requestContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }

  public RequestContext requestContext() {
    return requestContext;
  }

  public void webSocketContext(WebSocketContext webSocketContext) {
    this.webSocketContext = webSocketContext;
  }

  public WebSocketContext webSocketContext() {
    return webSocketContext;
  }

  public Throwable throwable() {
    return throwable;
  }

  public void throwable(Throwable throwable) {
    this.throwable = throwable;
  }
}
