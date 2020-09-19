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
package org.aquiver.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author WangYi
 * @since 2020/5/29
 */
public final class Async {
  private Async() {
  }

  /**
   * Returns if the future has successfully completed.
   */
  public static boolean isReady(CompletableFuture<?> future) {
    return (Objects.nonNull(future)) && future.isDone()
            && !future.isCompletedExceptionally()
            && (Objects.nonNull(future.join()));
  }

  /**
   * Returns the current value or null if either not done or failed.
   */
  public static <V> V getIfReady(CompletableFuture<V> future) {
    return isReady(future) ? future.join() : null;
  }

  /**
   * Returns the value when completed successfully or null if failed.
   */
  public static <V> V getWhenSuccessful(CompletableFuture<V> future) {
    try {
      return (Objects.isNull(future.join())) ? null : future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (ExecutionException e) {
      return null;
    }
  }
}
