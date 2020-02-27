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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author WangYi
 * @since 2019/6/14
 */
public class NettyThreadFactory implements ThreadFactory {

  private final String    prefix;
  private final LongAdder threadNumber = new LongAdder();

  public NettyThreadFactory(String prefix) {
    this.threadNumber.add(1);
    this.prefix = prefix;
  }

  /**
   * Constructs a new {@code Thread}.  Implementations may also initialize
   * priority, name, daemon status, {@code ThreadGroup}, etc.
   *
   * @param runnable a runnable to be executed by new thread instance
   * @return constructed thread, or {@code null} if the request to
   * create a thread is rejected
   */
  @Override
  public Thread newThread(Runnable runnable) {
    return new Thread(runnable, prefix + "thread-" + threadNumber.intValue());
  }
}
