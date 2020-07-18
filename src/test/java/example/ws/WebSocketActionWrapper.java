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
package example.ws;

import org.aquiver.mvc.annotation.bind.Param;
import org.aquiver.websocket.WebSocket;
import org.aquiver.websocket.WebSocketContext;
import org.aquiver.websocket.action.OnClose;
import org.aquiver.websocket.action.OnConnect;
import org.aquiver.websocket.action.OnError;
import org.aquiver.websocket.action.OnMessage;

/**
 * @author WangYi
 * @since 2020/7/13
 */
@WebSocket("/wrapper")
public class WebSocketActionWrapper {

  @OnConnect
  public void onConnect(WebSocketContext webSocketContext, @Param String name) {
    System.out.println("websocket open connect:" + name);
  }

  @OnMessage
  public void onMessage(WebSocketContext webSocketContext) {
    System.out.println(webSocketContext.getMessage().text());
    webSocketContext.message("测试消息回应");
  }

  @OnClose
  public void onClose(WebSocketContext webSocketContext) {
    System.out.println("关闭连接:" + webSocketContext.session().getId());
  }

  @OnError
  public void onError(WebSocketContext webSocketContext) {

  }
}
