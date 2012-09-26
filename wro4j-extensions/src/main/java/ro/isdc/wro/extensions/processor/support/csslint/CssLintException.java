/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import java.util.Collection;
import java.util.Collections;


/**
 * Exception caused by CssLint processing.
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 19 Jun 2011
 */
@SuppressWarnings("serial")
public class CssLintException extends Exception {
  private Collection<CssLintError> errors;

  /**
   * @return the errors
   */
  public Collection<CssLintError> getErrors() {
    if (errors == null) {
      return Collections.emptyList();
    }
    return this.errors;
  }


  /**
   * @param errors the errors to set
   */
  public CssLintException setErrors(final Collection<CssLintError> errors) {
    this.errors = errors;
    return this;
  }
}
