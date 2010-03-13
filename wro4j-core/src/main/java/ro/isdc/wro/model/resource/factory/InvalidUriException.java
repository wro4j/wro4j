/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Exception indicating that uri is invalid.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class InvalidUriException extends WroRuntimeException {
  /**
   * @param message
   * @param cause
   */
  public InvalidUriException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InvalidUriException(final String message) {
    super(message);
  }
}
