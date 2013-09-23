package ro.isdc.wro.runner.processor;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.model.resource.Resource;

/**
 * Custom extension of {@link CssLintProcessor} created for wro4j-runner.
 *
 * @author Alex Objelean
 * @since 1.7.2
 */
public class RunnerCssLintProcessor
    extends CssLintProcessor {
  /**
   * Override the alias of original CssLint procesor implementation.
   */
  public static String ALIAS = CssLintProcessor.ALIAS;

  @Override
  protected void onCssLintException(final CssLintException e, final Resource resource) {
    super.onCssLintException(e, resource);
    System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
    System.err.println(e.getErrors());
    throw e;
  }
}
