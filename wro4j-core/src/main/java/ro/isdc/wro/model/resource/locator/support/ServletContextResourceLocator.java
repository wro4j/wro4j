/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.StringUtils;


/**
 * {@link org.springframework.core.io.Resource} implementation for {@link javax.servlet.ServletContext} resources,
 * interpreting relative paths within the web application root directory.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ServletContextResourceLocator
    extends AbstractResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextResourceLocator.class);
  /**
   * Prefix used to identify if the path is a servlet context path.
   */
  public static final String PREFIX = "/";
  private final ServletContext servletContext;
  private final String path;

  public ServletContextResourceLocator(final ServletContext servletContext, final String path) {
    Validate.notNull(path);
    //allow null servletContext and prefer throwing IOException if null value is set.
    this.servletContext = servletContext;
    String pathToUse = StringUtils.cleanPath(path);
    if (!pathToUse.startsWith(PREFIX)) {
      pathToUse = PREFIX + pathToUse;
    }
    this.path = pathToUse;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
      throws IOException {
    if (servletContext == null) {
      throw new IOException("Cannot get stream for the following path: " + path + ", because no servletContext is detected.");
    }
    LOG.debug("locating uri: " + path);
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
      if (e instanceof NoMoreAttemptsIOException) {
        throw e;
      }
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
  String getPath() {
    return this.path;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator createRelative(final String relativePath)
      throws IOException {
    final String folder = FilenameUtils.getFullPath(path);
    // remove '../' & normalize the path.
    final String pathToUse = StringUtils.normalizePath(folder + relativePath);
    return new ServletContextResourceLocator(servletContext, pathToUse);
  }
}
