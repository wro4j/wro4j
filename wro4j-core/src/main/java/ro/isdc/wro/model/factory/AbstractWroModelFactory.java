/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
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

  /**
   * Override this method, in order to provide different xml definition file name.
   *
   * @return stream of the xml representation of the model.
   * @throws IOException if the stream couldn't be read.
   */
  protected InputStream getModelResourceAsStream()
    throws IOException {
    final ServletContext servletContext = Context.get().getServletContext();
    //Don't allow NPE, throw a more detailed exception
    if (servletContext == null) {
      throw new WroRuntimeException(
        "No servletContext is available. Probably you are running this code outside of the request cycle!");
    }
    return servletContext.getResourceAsStream("/WEB-INF/" + getDefaultModelFilename());
  }


  /**
   * Override this method, in order to provide different xml definition file name.
   *
   * @return stream of the xml representation of the model.
   */
  protected ResourceLocator getModelResourceLocator() {
    return new ServletContextResourceLocator(Context.get().getServletContext(), "/WEB-INF/" +getDefaultModelFilename());
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
