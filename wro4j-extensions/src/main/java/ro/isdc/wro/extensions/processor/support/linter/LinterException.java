/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.util.Collection;
import java.util.Collections;

import ro.isdc.wro.WroRuntimeException;


/**
 * Exception caused by JsHint processing.
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class LinterException extends WroRuntimeException {
  private Collection<LinterError> errors;

  public LinterException() {
    super("Linter error detected");
  }

  public LinterException(final String message, final Throwable cause) {
    super(message, cause);
  }


  /**
   * @return the errors
   */
  public Collection<LinterError> getErrors() {
    if (errors == null) {
      return Collections.emptyList();
    }
    return this.errors;
  }


  /**
   * @param errors the errors to set
   */
  public LinterException setErrors(final Collection<LinterError> errors) {
    this.errors = errors;
    return this;
  }
}
