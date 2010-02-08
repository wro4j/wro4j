/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WroRuntimeException.class);

  /**
   * @param message
   * @param cause
   */
  public WroRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
    LOG.error(message, cause);
  }

  /**
   * @param message
   */
  public WroRuntimeException(final String message) {
    super(message);
    LOG.error(message + Arrays.toString(this.getStackTrace()), this.getStackTrace());
  }
}
