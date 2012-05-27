/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro model using <a href="http://jslint.com/">jsLint</a>.
 *
 * @goal jslint
 * @phase compile
 * @requiresDependencyResolution runtime
 * @author Alex Objelean
 * @created 19 Sept 2011
 * @since 1.4.2
 */
public class JsLintMojo
    extends AbstractSingleProcessorMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceProcessor createResourceProcessor() {
    final ResourceProcessor processor = new JsLintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        getLog().info("processing resource: " + resource);
        if (resource != null) {
          getLog().info("processing resource: " + resource.getUri());
        }
        super.process(resource, reader, writer);
      }

      @Override
      protected void onException(final WroRuntimeException e) {
        JsLintMojo.this.onException(e);
      }

      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        getLog().error(
            e.getErrors().size() + " errors found while processing resource: " + resource.getUri() + " Errors are: "
                + e.getErrors());
        if (!isFailNever()) {
          //throw new MojoExecutionException("Errors found when validating resource: " + resource);
          throw new WroRuntimeException("Errors found when validating resource: " + resource);
        }
      };
    }.setOptions(getOptions());
    return processor;
  }

  /**
   * Used by unit test to check if mojo doesn't fail.
   */
  void onException(final Exception e) {
  }
}
