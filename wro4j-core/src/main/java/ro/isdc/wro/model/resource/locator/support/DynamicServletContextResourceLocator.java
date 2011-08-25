/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link org.springframework.core.io.Resource} implementation for {@link javax.servlet.ServletContext} resources,
 * interpreting relative paths within the web application root directory.
 *
 * @author Alex Objelean
 */
public class DynamicServletContextResourceLocator
    extends ServletContextResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(DynamicServletContextResourceLocator.class);
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  public DynamicServletContextResourceLocator(final HttpServletRequest request, final HttpServletResponse response,
      final ServletContext servletContext, final String path) {
    super(servletContext, path);
    if (request == null) {
      throw new IllegalArgumentException("Request cannot be null!");
    }
    if (response == null) {
      throw new IllegalArgumentException("Response cannot be null!");
    }
    this.request = request;
    this.response = response;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream getInputStream()
      throws IOException {
    /**
     * Locator of dynamic resources. There can be different strategies. We will always use only this. Try to switch
     * later to see if performance change.
     */
    InputStream inputStream = null;
    try {
      inputStream = new DispatcherStreamLocator().getInputStream(request, response, getPath());
    } catch (final IOException e) {
      if (inputStream == null) {
        // second attempt
        inputStream = super.getInputStream();
      }
      if (inputStream == null) {
        LOG.error("Exception while reading resource from " + getPath());
        throw new IOException("Exception while reading resource from " + getPath());
      }
    }
    return inputStream;
  }
}
