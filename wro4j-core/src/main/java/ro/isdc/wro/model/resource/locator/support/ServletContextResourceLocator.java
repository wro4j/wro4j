/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.StringUtils;


/**
 * {@link org.springframework.core.io.Resource} implementation for {@link javax.servlet.ServletContext} resources,
 * interpreting relative paths within the web application root directory.
 *
 * @author Alex Objelean
 */
public class ServletContextResourceLocator extends AbstractResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextResourceLocator.class);
  /**
   * Locator of dynamic resources. There can be different strategies. We will always use only this. Try to switch later
   * to see if performance change.
   */
  private final ByteArrayStreamDispatchingStrategy dynamicStreamLocator = new ByteArrayStreamDispatchingStrategy();

  private final ServletContext servletContext;
  private final String path;

  public ServletContextResourceLocator(final ServletContext servletContext, final String path) {
    if (servletContext == null) {
      throw new IllegalArgumentException("ServletContext cannot be null!");
    }
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null!");
    }
    this.servletContext = servletContext;
    String pathToUse = StringUtils.cleanPath(path);
    if (!pathToUse.startsWith("/")) {
      pathToUse = "/" + pathToUse;
    }
    this.path = pathToUse;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
    throws IOException {
    LOG.debug("locating uri: " + path);
    if (path == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    LOG.debug("uri resource: " + path);
    try {
      if (getWildcardStreamLocator().hasWildcard(path)) {
        final String fullPath = FilenameUtils.getFullPath(path);
        final String realPath = servletContext.getRealPath(fullPath);
        if (realPath == null) {
          final String message = "Could not determine realPath for resource: " + path;
          LOG.error(message);
          throw new IOException(message);
        }
        return getWildcardStreamLocator().locateStream(path, new File(realPath));
      }
    } catch (final IOException e) {
      LOG.warn("Couldn't localize the stream containing wildcard. Original error message: \"" + e.getMessage()
        + "\".\n Trying to locate the stream without the wildcard.");
    }

    // first attempt
    final InputStream inputStream = servletContext.getResourceAsStream(path);
    if (inputStream == null) {
      LOG.error("Exception while reading resource from " + path);
      throw new IOException("Exception while reading resource from " + path);
    }
    return inputStream;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return this.path;
  }

}
