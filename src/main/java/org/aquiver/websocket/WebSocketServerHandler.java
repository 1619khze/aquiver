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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import org.aquiver.Aquiver;
import org.aquiver.RequestContext;
import org.aquiver.mvc.route.RouteManager;
import org.aquiver.utils.StringUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author WangYi
 * @since 2020/7/5
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
  private WebSocketServerHandshaker handshaker;
  private WebSocketChannel webSocketChannel;
  private WebSocketSession webSocketSession;
  private final RouteManager routeManager;
  private RequestContext requestContext;

  public WebSocketServerHandler() {
    Aquiver of = Aquiver.of();
    this.routeManager = of.routeManager();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    } else {
      ReferenceCountUtil.retain(msg);
      ctx.fireChannelRead(msg);
    }
  }

  @Override
  public void channelReadComplete(final ChannelHandlerContext ctx) {
    ctx.flush();
  }

  /**
   * Handler http request
   *
   * @param ctx Netty channel context
   * @param req An HTTP request.
   */
  private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {
    DefaultFullHttpRequest defaultFullHttpRequest =
            new DefaultFullHttpRequest(req.protocolVersion(), req.method(), req.uri());
    this.requestContext = new RequestContext(defaultFullHttpRequest, ctx);
    if (isWebSocketRequest(req)) {
      final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
              req.uri(), null, true);
      this.handshaker = wsFactory.newHandshaker(req);
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
      } else {
        handshaker.handshake(ctx.channel(), req);

        long id = ThreadLocalRandom.current().nextLong();
        this.webSocketSession = new WebSocketSession(ctx, String.valueOf(id)
                .replaceFirst("-", ""));
        CompletableFuture.completedFuture(webSocketContext())
                .thenAcceptAsync(session -> webSocketChannel
                        .onConnect(session), ctx.executor());
      }
    } else {
      ReferenceCountUtil.retain(req);
      ctx.fireChannelRead(req);
    }
  }

  private WebSocketContext webSocketContext() {
    return WebSocketContext.create(requestContext, webSocketSession, webSocketChannel);
  }

  /**
   * Handle websocket requests
   *
   * @param ctx   Netty channel context
   * @param frame Base class for web socket frames.
   */
  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    if (frame instanceof CloseWebSocketFrame) {
      handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
      CompletableFuture.completedFuture(webSocketContext()).thenAcceptAsync(webSocketChannel::onClose);
      return;
    }

    if (frame instanceof PingWebSocketFrame) {
      ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
      return;
    }

    if (!(frame instanceof TextWebSocketFrame)) {
      final RuntimeException throwable =
              new UnsupportedOperationException("Unsupported frame type [" + frame.getClass().getName() + "]");

      this.handshaker.close(ctx.channel(), new CloseWebSocketFrame());

      WebSocketContext webSocketContext = webSocketContext();
      webSocketContext.setError(new WebSocketContext.Error(throwable, webSocketContext()));
      CompletableFuture.completedFuture(webSocketContext)
              .thenAcceptAsync(webSocketChannel::onError, ctx.executor());
      return;
    }
    WebSocketContext webSocketContext = webSocketContext();
    webSocketContext.setMessage(new WebSocketContext
            .Message(((TextWebSocketFrame) frame).text(), webSocketContext()));

    CompletableFuture.completedFuture(webSocketContext)
            .thenAcceptAsync(webSocketChannel::onMessage, ctx.executor());
  }

  /**
   * Determine if it is a websocket request
   *
   * @param req An HTTP request.
   * @return Is it websocket request
   */
  private boolean isWebSocketRequest(HttpRequest req) {
    String uri = StringUtils.substringBefore(req.uri(), "?");
    return Objects.nonNull(req)
            && Objects.nonNull((this.webSocketChannel = this.routeManager.getWebSockets().get(uri)))
            && req.decoderResult().isSuccess()
            && "websocket".equals(req.headers().get("Upgrade"));
  }

  /**
   * Calls {@link ChannelHandlerContext#fireUserEventTriggered(Object)} to forward
   * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
   * <p>
   * Sub-classes may override this method to change behavior.
   */
  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    if (null != webSocketSession && null != webSocketChannel) {
      WebSocketContext webSocketContext = webSocketContext();
      webSocketContext.setError(new WebSocketContext.Error(cause, webSocketContext()));
      CompletableFuture.completedFuture(webSocketContext)
              .thenAcceptAsync(webSocketChannel::onError, ctx.executor());
    }
    ctx.close();
  }
}
