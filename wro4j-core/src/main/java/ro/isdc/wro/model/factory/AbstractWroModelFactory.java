/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletContext;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;


/**
 * To be used by the implementations which load the model from a resource provided as stream.
 * 
 * @author Alex Objelean
 * @since 1.4.0
 */
public abstract class AbstractWroModelFactory
    implements WroModelFactory {
  @Inject
  private ReadOnlyContext context;
  
  /**
   * Override this method, in order to provide different xml definition file name.
   * 
   * @return stream of the xml representation of the model.
   * @throws IOException
   *           if the stream couldn't be read.
   */
  protected InputStream getModelResourceAsStream()
      throws IOException {
    final ServletContext servletContext = context.getServletContext();
    // Don't allow NPE, throw a more detailed exception
    if (servletContext == null) {
      throw new WroRuntimeException(
          "No servletContext is available. Probably you are running this code outside of the request cycle!");
    }
    final String resourceLocation = "/WEB-INF/" + getDefaultModelFilename();
    final InputStream stream = servletContext.getResourceAsStream(resourceLocation);
    if (stream == null) {
      throw new IOException("Invalid resource requested: " + resourceLocation);
    }
    return stream;
  }
  
  /**
   * @return the default name of the file describing the wro model.
   */
  protected abstract String getDefaultModelFilename();
  
  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }
  
}
