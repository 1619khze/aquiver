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

/**
 * @author WangYi
 * @since 2019/5/9
 */
public class NettyServerGroup {
  private EventLoopGroup                 bossGroup;
  private EventLoopGroup                 workGroup;
  private Class<? extends ServerChannel> channelClass;

  NettyServerGroup(EventLoopGroup bossGroup, EventLoopGroup workGroup, Class<? extends ServerChannel> channelClass) {
    this.bossGroup    = bossGroup;
    this.workGroup    = workGroup;
    this.channelClass = channelClass;
  }

  public static NettyServerGroupBuilder builder() {
    return new NettyServerGroupBuilder();
  }

  EventLoopGroup getBossGroup() {
    return bossGroup;
  }

  EventLoopGroup getWorkGroup() {
    return workGroup;
  }

  Class<? extends ServerChannel> getChannelClass() {
    return channelClass;
  }

  public static class NettyServerGroupBuilder {
    private EventLoopGroup                 bossGroup;
    private EventLoopGroup                 workGroup;
    private Class<? extends ServerChannel> channelClass;

    NettyServerGroupBuilder() {
    }

    public NettyServerGroup.NettyServerGroupBuilder bossGroup(EventLoopGroup bossGroup) {
      this.bossGroup = bossGroup;
      return this;
    }

    public NettyServerGroup.NettyServerGroupBuilder workGroup(EventLoopGroup workGroup) {
      this.workGroup = workGroup;
      return this;
    }

    public NettyServerGroup.NettyServerGroupBuilder channelClass(Class<? extends ServerChannel> channelClass) {
      this.channelClass = channelClass;
      return this;
    }

    public NettyServerGroup build() {
      return new NettyServerGroup(this.bossGroup, this.workGroup, this.channelClass);
    }
  }
}
