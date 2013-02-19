/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.StringUtils;


/**
 * {@link org.springframework.core.io.Resource} implementation for {@link javax.servlet.ServletContext} resources,
 * interpreting relative paths within the web application root directory.
 *
 * @author Alex Objelean, Ivar Conradi Østhus
 * @created Created on Nov 10, 2008, Updated on March 2, 2012
 */
public class ServletContextResourceLocator
    extends AbstractResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextResourceLocator.class);
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "servletContext";
  /**
   * Same as default Alias (exist for explicit configuration). Uses DISPATCHER_FIRST strategy. Meaning that, for
   * example, a jsp resource will be served in its final state (processed by servlet container), rather than in its raw
   * variant.
   */
  public static final String ALIAS_DISPATCHER_FIRST = "servletContext.DISPATCHER_FIRST";
  /**
   * Uses SERVLET_CONTEXT_FIRST strategy, meaning that, for example, a jsp will be served with its raw content, instead
   * of processed by container.
   */
  public static final String ALIAS_SERVLET_CONTEXT_FIRST = "servletContext.SERVLET_CONTEXT_FIRST";

  /**
   * Prefix for url resources.
   */
  public static final String PREFIX = "/";
  private final ServletContext servletContext;
  private final String path;
  /**
   * Locates a stream using request dispatcher.
   */
  private DispatcherStreamLocator dispatcherStreamLocator;
  /**
   * Determines the order of dispatcher resource locator and servlet context based resource locator.
   */
  private LocatorStrategy locatorStrategy = LocatorStrategy.DISPATCHER_FIRST;
  /**
   * Available LocatorStrategies. DISPATCHER_FIRST is default option. This means this UriLocator will first try to
   * locate resource via the dispatcher stream locator. This will include dynamic resources produces by servlet's or
   * JSP's. If the specified resource cannot be found with the dispatcherStreamLocator the implementation will try to
   * use the ServletContext to locate the resource. SERVLET_CONTEXT_FIRST is a alternative approach where we will first
   * try to locate the resource VIA the ServletContext first, and then use the dispatcheStreamLocator if not found. In
   * some cases, where you do not rely on dynamic resources this can be a more reliable and a more efficient approach.
   */
  public static enum LocatorStrategy {
    DISPATCHER_FIRST, SERVLET_CONTEXT_FIRST
  }

  /**
   * Sets the locator strategy to use.
   */
  public ResourceLocator setLocatorStrategy(final LocatorStrategy locatorStrategy) {
    Validate.notNull(locatorStrategy);
    this.locatorStrategy = locatorStrategy;
    return this;
  }


  public ServletContextResourceLocator(final ServletContext servletContext, final String path) {
    Validate.notNull(path);
    // allow null servletContext and prefer throwing IOException if null value is set.
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
      throw new IOException("Cannot get stream for the following path: " + path
          + ", because no servletContext is detected.");
    }
    LOG.debug("locating uri: {}", path);
    try {
      if (getWildcardStreamLocator().hasWildcard(path)) {
        final String fullPath = FilenameUtils.getFullPath(path);
        final String realPath = servletContext.getRealPath(fullPath);
        if (realPath == null) {
          final String message = "[FAIL] Could not determine realPath for resource: " + path;
          LOG.debug(message);
          throw new IOException(message);
        }
        return getWildcardStreamLocator().locateStream(path, new File(realPath));
      }
    } catch (final IOException e) {
      /**
       * This is a special case when no more attempts are required, since the required computation was achieved
       * successfully. This solves the following <a
       * href="http://code.google.com/p/wro4j/issues/detail?id=321">issue</a>.
       * <p/>
       * The problem was that in some situations, when the dispatcherStreamLocator was used to locate resources
       * containing wildcard, the following message was printed to the console:
       * <code>SEVERE: Servlet.service() for servlet default threw exception
       * java.io.FileNotFoundException.</code>
       */
      if (e instanceof NoMoreAttemptsIOException) {
        throw e;
      }
      LOG.debug("[FAIL] localize the stream containing wildcard. Original error message: '{}'", e.getMessage()
          + "\".\n Trying to locate the stream without the wildcard.");
    }

    InputStream inputStream = null;
    try {
      if (locatorStrategy.equals(LocatorStrategy.DISPATCHER_FIRST)) {
        inputStream = dispatcherFirstStreamLocator(path);
      } else {
        inputStream = servletContextFirstStreamLocator(path);
      }
      validateInputStreamIsNotNull(inputStream, path);
      return inputStream;
    } catch (final IOException e) {
      LOG.debug("Wrong or empty resource with location: {}", path);
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator createRelative(final String relativePath)
      throws IOException {
    final String folder = FilenameUtils.getFullPath(path);
    // remove '../' & normalize the path.
    final String pathToUse = StringUtils.cleanPath(folder + relativePath);
    return new ServletContextResourceLocator(servletContext, pathToUse);
  }


  private InputStream servletContextFirstStreamLocator(final String uri)
      throws IOException {
    try {
      return servletContextBasedStreamLocator(uri);
    } catch (final IOException e) {
      LOG.debug("retrieving servletContext stream for uri: {}", uri);
      return dispatcherBasedStreamLocator(uri);
    }
  }

  private InputStream dispatcherFirstStreamLocator(final String uri)
      throws IOException {
    try {
      return dispatcherBasedStreamLocator(uri);
    } catch (final IOException e) {
      LOG.debug("retrieving servletContext stream for uri: {}", uri);
      return servletContextBasedStreamLocator(uri);
    }
  }

  private InputStream dispatcherBasedStreamLocator(final String uri)
      throws IOException {
    final Context context = Context.get();
    final HttpServletRequest request = context.getRequest();
    final HttpServletResponse response = context.getResponse();
    // The order of stream retrieval is important. We are trying to get the dispatcherStreamLocator in order to handle
    // jsp resources (if such exist). Switching the order would cause jsp to not be interpreted by the container.
    return new DispatcherStreamLocator().getInputStream(request, response, uri);
  }

  private InputStream servletContextBasedStreamLocator(final String uri)
      throws IOException {
    try {
      return Context.get().getServletContext().getResourceAsStream(uri);
    } catch (final Exception e) {
      throw new IOException("Could not locate uri: " + uri, e);
    }
  }

  private void validateInputStreamIsNotNull(final InputStream inputStream, final String uri)
      throws IOException {
    if (inputStream == null) {
      LOG.debug("[FAIL] reading resource from {}", uri);
      throw new IOException("Exception while reading resource from " + uri);
    }
  }
}
