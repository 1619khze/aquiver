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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ThreadFactory;

/**
 * @author WangYi
 * @since 2019/6/6
 */
public final class EventLoopKit {
  private static EventLoopGroup bossGroup;
  private static EventLoopGroup workerGroup;
  private static final Class<? extends ServerChannel> epollChannel = EpollServerSocketChannel.class;
  private static final Class<? extends ServerChannel> nioChannel = NioServerSocketChannel.class;

  private EventLoopKit() {}

  public static boolean epollIsAvailable() {
    try {
      Object obj = Class.forName("io.netty.channel.epoll.Epoll")
              .getMethod("isAvailable").invoke(null);
      return null != obj && Boolean.parseBoolean(obj.toString())
              && System.getProperty("os.name").toLowerCase().contains("linux");
    } catch (Exception e) {
      return false;
    }
  }

  public static NettyServerGroup epollGroup(int threadCount, int workers) {
    bossGroup = new EpollEventLoopGroup(threadCount, named("epoll-boss@"));
    workerGroup = new EpollEventLoopGroup(workers, named("epoll-worker@"));
    return eventLoopGroupBuilder(bossGroup, workerGroup, epollChannel);
  }

  public static NettyServerGroup nioGroup(int threadCount, int workers) {
    bossGroup = new NioEventLoopGroup(threadCount, named("nio-boss@"));
    workerGroup = new NioEventLoopGroup(workers, named("nio-worker@"));
    return eventLoopGroupBuilder(bossGroup, workerGroup, nioChannel);
  }

  private static NettyServerGroup eventLoopGroupBuilder(
          EventLoopGroup bossEventLoopGroup, EventLoopGroup workerEventLoopGroup,
          Class<? extends ServerChannel> channelClass) {
    return NettyServerGroup.builder().bossGroup(bossEventLoopGroup)
            .workGroup(workerEventLoopGroup).channelClass(channelClass).build();
  }

  private static ThreadFactory named(String named) {
    return new NettyThreadFactory(named);
  }

  /**
   * judge io mode
   *
   * @param channelName
   * @return
   */
  public static String judgeMode(String channelName) {
    String NIO = "nio";
    String EPOLL = "epoll";
    if (channelName.toLowerCase().startsWith(NIO)) {
      return NIO;
    } else if (channelName.toLowerCase().startsWith(EPOLL)) {
      return EPOLL;
    }
    return NIO;
  }
}
