/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;


/**
 * Base Wro Runtime exception. All exceptions will extend this runtime exception.
 *
 * @author Alex Objelean
 */
public class WroRuntimeException
    extends RuntimeException {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(WroRuntimeException.class);
  private Resource resource;

  public WroRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
    LOG.debug(message);
  }

  /**
   * @param message
   */
  public WroRuntimeException(final String message) {
    this(message, null);
  }

  /**
   * Logs the error of this exception. By default errors are logged with DEBUG level. This method will use ERROR level.
   */
  public WroRuntimeException logError() {
    LOG.error(getMessage());
    return this;
  }

  /**
   * Wraps original exception into {@link WroRuntimeException} and throw it.
   *
   * @param e
   *          the exception to wrap.
   */
  public static WroRuntimeException wrap(final Exception e) {
    return wrap(e, e.getMessage());
  }

  /**
   * Wraps original exception into {@link WroRuntimeException} and throw it.
   *
   * @param e
   *          the exception to wrap.
   * @param message
   *          the message of the exception to wrap.
   */
  public static WroRuntimeException wrap(final Exception e, final String message) {
    if (e instanceof WroRuntimeException) {
      return (WroRuntimeException) e;
    }
    return new WroRuntimeException(message, e);
  }

  /**
   * @param resource
   *          The resource being processed when exception occurs.
   */
  public WroRuntimeException setResource(final Resource resource) {
    this.resource = resource;
    return this;
  }

  /**
   * @return The processed {@link Resource} which caused the exception.
   */
  public Resource getResource() {
    return resource;
  }
}
