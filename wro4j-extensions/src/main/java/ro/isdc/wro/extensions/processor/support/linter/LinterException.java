/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.util.Collection;
import java.util.Collections;


/**
 * Exception caused by JsHint processing.
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class LinterException extends Exception {
  private Collection<LinterError> errors;

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
