package org.aquiver;

public class HandlerRepeatException extends RuntimeException {

  public HandlerRepeatException(String message) {
    super(message);
  }

  public HandlerRepeatException(String message, Throwable cause) {
    super(message, cause);
  }
}
