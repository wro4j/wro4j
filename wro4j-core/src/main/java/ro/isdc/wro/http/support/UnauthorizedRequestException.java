/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http.support;

import ro.isdc.wro.WroRuntimeException;


/**
 * This exception is thrown when an invalid group is requested..
 *
 * @author Alex Objelean
 */
public class UnauthorizedRequestException extends WroRuntimeException {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param message detailed message.
   */
  public UnauthorizedRequestException(final String message) {
    super(message);
  }
}
