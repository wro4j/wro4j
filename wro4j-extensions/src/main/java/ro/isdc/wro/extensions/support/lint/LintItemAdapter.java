package ro.isdc.wro.extensions.support.lint;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;

/**
 * Adapts various objects to {@link LintItem}
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class LintItemAdapter
    extends LintItem {
  /**
   * Adapts a {@link LinterError} into LintItem.
   */
  public LintItemAdapter(final LinterError linterError) {
    Validate.notNull(linterError);
    setColumn(linterError.getCharacter());
    setLine(linterError.getLine());
    setEvidence(linterError.getEvidence());
    setReason(linterError.getReason());
  }
  
  /**
   * Adapts a {@link LinterError} into LintItem.
   */
  public LintItemAdapter(final CssLintError cssLintError) {
    Validate.notNull(cssLintError);
    setColumn(cssLintError.getCol());
    setLine(cssLintError.getLine());
    setEvidence(cssLintError.getEvidence());
    setReason(cssLintError.getMessage());
    setSeverity(cssLintError.getType());
  }
}
