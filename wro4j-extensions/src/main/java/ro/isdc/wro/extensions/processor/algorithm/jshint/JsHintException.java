/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.algorithm.jshint;

import java.util.Collection;
import java.util.Collections;


/**
 * @author Alex Objelean
 */
public class JsHintException extends Exception {
  private Collection<JsError> errors;

  /**
   * @return the errors
   */
  public Collection<JsError> getErrors() {
    if (errors == null) {
      return Collections.EMPTY_LIST;
    }
    return this.errors;
  }


  /**
   * @param errors the errors to set
   */
  public JsHintException setErrors(final Collection<JsError> errors) {
    this.errors = errors;
    return this;
  }
}
