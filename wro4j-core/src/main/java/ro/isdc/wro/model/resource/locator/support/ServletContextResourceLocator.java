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

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.StringUtils;


/**
 * {@link org.springframework.core.io.Resource} implementation for {@link javax.servlet.ServletContext} resources,
 * interpreting relative paths within the web application root directory.
 * 
 * @author Alex Objelean, Ivar Conradi Østhus
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
  /**
   * Locates a stream using request dispatcher.
   */
  private final DispatcherStreamLocator dispatcherStreamLocator = new DispatcherStreamLocator();  
  /**
   * Determines the order of dispatcher resource locator and servlet context based resource locator.
   */
  private LocatorStrategy locatorStrategy = LocatorStrategy.DISPATCHER_FIRST;
  @Inject
  private ReadOnlyContext context;
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
    LOG.debug("locating uri: " + path);
    try {
      if (getWildcardStreamLocator().hasWildcard(path)) {
        final String fullPath = FilenameUtils.getFullPath(path);
        final String realPath = servletContext.getRealPath(fullPath);
        if (realPath == null) {
          final String message = "[FAIL] Could not determine realPath for resource: " + path;
          LOG.error(message);
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
      LOG.warn("[FAIL] Couldn't localize the stream containing wildcard. Original error message: \"" + e.getMessage()
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
    } catch (IOException e) {
      LOG.warn("Wrong or empty resource with location: {}", path);
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
    final HttpServletRequest request = context.getRequest();
    final HttpServletResponse response = context.getResponse();
    // The order of stream retrieval is important. We are trying to get the dispatcherStreamLocator in order to handle
    // jsp resources (if such exist). Switching the order would cause jsp to not be interpreted by the container.
    return dispatcherStreamLocator.getInputStream(request, response, uri);
  }
  
  private InputStream servletContextBasedStreamLocator(final String uri)
      throws IOException {
    final ServletContext servletContext = context.getServletContext();
    return servletContext.getResourceAsStream(uri);
  }
  
  private void validateInputStreamIsNotNull(final InputStream inputStream, final String uri)
      throws IOException {
    if (inputStream == null) {
      LOG.error("[FAIL] reading resource from " + uri);
      throw new IOException("Exception while reading resource from " + uri);
    }
  }
}
