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
package org.aquiver.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.ssl.SslContext;
import org.aquiver.Aquiver;
import org.aquiver.websocket.WebSocketHandler;

import java.util.Objects;

/**
 * @author WangYi
 * @since 2019/6/5
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
  private final SslContext sslCtx;
  private final Aquiver aquiver = Aquiver.of();

  NettyServerInitializer(SslContext sslCtx) {
    this.sslCtx = sslCtx;
  }

  /**
   * init webSocket and bind netty childHandler, Determine whether SSL needs to
   * be enabled based on whether the incoming SSLContext is null
   * @param ch A TCP/IP socket
   */
  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline channelPipeline = ch.pipeline();
    if (Objects.nonNull(sslCtx)) {
      channelPipeline.addLast(sslCtx.newHandler(ch.alloc()));
    }
    if (aquiver.cors()) {
      CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
              .allowNullOrigin().allowCredentials().build();
      channelPipeline.addLast(new CorsHandler(corsConfig));
    }
    if (aquiver.gzip()) {
      channelPipeline.addLast(new HttpContentCompressor());
    }
    channelPipeline.addLast(new HttpServerCodec());
    channelPipeline.addLast(new HttpServerExpectContinueHandler());
    channelPipeline.addLast(new WebSocketHandler());
    channelPipeline.addLast(new NettyServerHandler());
  }
}
