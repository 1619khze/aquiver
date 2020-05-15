package org.aquiver;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

public class AquiverThreadFactory implements ThreadFactory {

  private final String prefix;
  private final LongAdder threadNumber = new LongAdder();

  public AquiverThreadFactory(String prefix) {
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
