/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro model.
 *
 * @goal jshint
 * @phase process-resources
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHintMojo extends AbstractSingleProcessorMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new JsHintProcessor() {
      @Override
      protected void onJsHintException(final JsHintException e, final Resource resource)
        throws Exception {
        getLog().error(
          e.getErrors().size() + " errors found while processing resource: " + resource.getUri() + " Errors are: "
            + e.getErrors());
        if (!isFailNever()) {
          throw new MojoExecutionException("Errors found when validating resource: " + resource);
        }
      };
    }.setOptions(getOptions());
    return processor;
  }
}
