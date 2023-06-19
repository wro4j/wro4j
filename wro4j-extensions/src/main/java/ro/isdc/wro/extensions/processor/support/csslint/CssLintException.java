/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import java.util.Collection;
import java.util.Collections;

import ro.isdc.wro.WroRuntimeException;


/**
 * Exception caused by CssLint processing.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
@SuppressWarnings("serial")
public class CssLintException extends WroRuntimeException {
  private Collection<CssLintError> errors;

  public CssLintException() {
    super("CssLint error found");
  }

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
