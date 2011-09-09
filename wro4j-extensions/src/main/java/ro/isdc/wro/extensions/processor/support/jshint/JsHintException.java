/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.jshint;

import java.util.Collection;
import java.util.Collections;


/**
 * Exception caused by JsHint processing.
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHintException extends Exception {
  private Collection<JsHintError> errors;

  /**
   * @return the errors
   */
  public Collection<JsHintError> getErrors() {
    if (errors == null) {
      return Collections.EMPTY_LIST;
    }
    return this.errors;
  }


  /**
   * @param errors the errors to set
   */
  public JsHintException setErrors(final Collection<JsHintError> errors) {
    this.errors = errors;
    return this;
  }
}
