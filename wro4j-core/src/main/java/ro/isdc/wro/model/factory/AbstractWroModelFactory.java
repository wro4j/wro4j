/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;

/**
 * To be used by the implementations which load the model from a resource provided as stream.
 *
 * @author Alex Objelean
 * @created 9 Aug 2011
 * @since 1.4.0
 */
public abstract class AbstractWroModelFactory
  implements WroModelFactory {
  @Inject
  private Context context;
  /**
   * Override this method, in order to provide different xml definition file name.
   *
   * @return stream of the xml representation of the model.
   * @throws IOException
   *           if the stream couldn't be read.
   */
  protected ResourceLocator getModelResourceLocator() {
    return new ServletContextResourceLocator(Context.get().getServletContext(), "/WEB-INF/" + getDefaultModelFilename());
  }

  /**
   * @return the default name of the file describing the wro model.
   */
  protected abstract String getDefaultModelFilename();

  /**
   * {@inheritDoc}
   */
  public void destroy() {}

}
