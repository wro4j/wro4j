/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.maven.plugin.MojoExecutionException;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate css code defined in wro model.
 *
 * @goal csslint
 * @phase compile
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 20 Jun 2011
 */
public class CssLintMojo extends AbstractSingleProcessorMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new CssLintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
        if (resource != null) {
          getLog().info("processing resource: " + resource.getUri());
        }
        super.process(resource, reader, writer);
      }

      @Override
      protected void onCssLintException(final CssLintException e, final Resource resource) throws Exception {
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
