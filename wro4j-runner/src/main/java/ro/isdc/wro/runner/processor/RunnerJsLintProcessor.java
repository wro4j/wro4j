package ro.isdc.wro.runner.processor;

import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;


/**
 * Custom extension of {@link JsLintProcessor} created for wro4j-runner.
 *
 * @author Alex Objelean
 * @since 1.7.2
 */
public class RunnerJsLintProcessor
    extends JsLintProcessor {
  /**
   * Override the alias of original jsLint processor implementation.
   */
  public static String ALIAS = JsLintProcessor.ALIAS;
  @Override
  protected void onLinterException(final LinterException e, final Resource resource) {
    super.onLinterException(e, resource);
    System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
    System.err.println(e.getErrors());
    throw e;
  }
}
