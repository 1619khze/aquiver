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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.ssl.SslContext;
import org.aquiver.RouteResolver;
import org.aquiver.Const;
import org.aquiver.Environment;

/**
 * @author WangYi
 * @since 2019/6/5
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
  private final RouteResolver routeResolver;
  private final SslContext    sslCtx;
  private       boolean     cors;
  private       boolean     compressor;
  private       CorsConfig  corsConfig;

  NettyServerInitializer(SslContext sslCtx, Environment environment, RouteResolver routeResolver) {
    this.sslCtx = sslCtx;
    final Boolean cors = environment.getBoolean(Const.PATH_SERVER_CORS, Const.SERVER_CORS);
    final Boolean gzip = environment.getBoolean(Const.PATH_SERVER_CONTENT_COMPRESSOR, Const.SERVER_CONTENT_COMPRESSOR);
    this.cors(cors).gzip(gzip);
    this.routeResolver = routeResolver;
  }

  private NettyServerInitializer cors(boolean cors) {
    this.cors       = cors;
    this.corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
    return this;
  }

  private NettyServerInitializer gzip(boolean compressor) {
    this.compressor = compressor;
    return this;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline channelPipeline = ch.pipeline();
    if (sslCtx != null) channelPipeline.addLast(sslCtx.newHandler(ch.alloc()));
    if (cors) channelPipeline.addLast(new CorsHandler(corsConfig));
    if (compressor) channelPipeline.addLast(new HttpContentCompressor());
    channelPipeline.addLast(new HttpRequestDecoder());
    channelPipeline.addLast(new HttpResponseEncoder());
    channelPipeline.addLast(new HttpObjectAggregator(65536));
    channelPipeline.addLast(new NettyServerHandler(routeResolver));
  }
}
