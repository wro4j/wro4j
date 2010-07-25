/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro;


/**
 * Base Wro Runtime exception. All exceptions will extend this runtime
 * exception.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class WroRuntimeException extends RuntimeException {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   * @param cause
   */
  public WroRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public WroRuntimeException(final String message) {
    super(message);
  }
}
