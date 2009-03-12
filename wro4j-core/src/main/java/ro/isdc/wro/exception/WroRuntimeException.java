/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base Wro Runtime exception. All exceptions will extend this runtime
 * exception.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
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
  private static final Log log = LogFactory.getLog(WroRuntimeException.class);

  /**
   * Default constructor.
   */
  public WroRuntimeException() {
    super();
    log.error("Error occured");
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public WroRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
    log.error(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public WroRuntimeException(final String message) {
    super(message);
    log.error(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public WroRuntimeException(final Throwable cause) {
    super(cause);
    log.error(cause);
    // TODO Auto-generated constructor stub
  }
}
