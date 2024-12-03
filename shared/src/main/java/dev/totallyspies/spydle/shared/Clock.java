package dev.totallyspies.spydle.shared;

import java.util.concurrent.atomic.AtomicReference;

/*
 * Using a Singleton pattern to override currentTimeMillis when testing.
 */
public class Clock {
  private static final AtomicReference<Clock> clock = new AtomicReference<>();

  public static Clock getInstance() {
    clock.compareAndSet(null, new Clock());
    return clock.get();
  }

  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }
}
