/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.WroUtil;


/**
 * <p>UriLocator capable to read the resources relative to servlet context. The resource reader will attempt to locate a
 * physic resource under the servlet context and if the resource does not exist, will try to use requestDispatcher. This
 * kind of resources will be accepted if their prefix is '/'.</p>
 *
 * <p>This locator can be used only within a wro4j request cycle. In other words, the {@link Context} instance should be
 * available in the {@link ThreadLocal} associated with the thread invoking this locator.</p>
 *
 * @author Alex Objelean, Ivar Conradi Østhus
 */
public class ServletContextUriLocator
    extends WildcardUriLocatorSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ServletContextUriLocator.class);
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
   * Uses SERVLET_CONTEXT_ONLY strategy, meaning that no dispatching will be performed when there is no servletContext
   * resource available.
   */
  public static final String ALIAS_SERVLET_CONTEXT_ONLY = "servletContext.SERVLET_CONTEXT_ONLY";

  /**
   * Prefix for url resources.
   */
  public static final String PREFIX = "/";
  /**
   * Constant for WEB-INF folder.
   */
  private static final String PROTECTED_PREFIX = "/WEB-INF/";
  /**
   * Determines the order of dispatcher resource locator and servlet context based resource locator.
   */
  private LocatorStrategy locatorStrategy = LocatorStrategy.DISPATCHER_FIRST;
  @Inject
  private DispatcherStreamLocator dispatcherLocator;

  /**
   * Available LocatorStrategies. DISPATCHER_FIRST is default option. This means this UriLocator will first try to
   * locate resource via the dispatcher stream locator. This will include dynamic resources produces by servlet's or
   * JSP's. If the specified resource cannot be found with the dispatcherStreamLocator the implementation will try to
   * use the ServletContext to locate the resource. SERVLET_CONTEXT_FIRST is a alternative approach where we will first
   * try to locate the resource VIA the ServletContext first, and then use the dispatcheStreamLocator if not found. In
   * some cases, where you do not rely on dynamic resources this can be a more reliable and a more efficient approach.
   * If requests should never be forwarded to a servlet, use SERVLET_CONTEXT_ONLY.
   */
  public static enum LocatorStrategy {
    DISPATCHER_FIRST, SERVLET_CONTEXT_FIRST, SERVLET_CONTEXT_ONLY
  }

  /**
   * Sets the locator strategy to use.
   */
  public ServletContextUriLocator setLocatorStrategy(final LocatorStrategy locatorStrategy) {
    notNull(locatorStrategy);
    this.locatorStrategy = locatorStrategy;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return isValid(uri);
  }

  /**
   * Check if a uri is a servletContext resource.
   *
   * @param uri
   *          to check.
   * @return true if the uri is a servletContext resource.
   */
  public static boolean isValid(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }

  /**
   * Check If the uri of the resource is protected: it cannot be accessed by accessing the url directly (WEB-INF
   * folder).
   *
   * @param uri
   *          the uri to check.
   * @return true if the uri is a protected resource.
   */
  public static boolean isProtectedResource(final String uri) {
    return WroUtil.startsWithIgnoreCase(uri, PROTECTED_PREFIX);
  }

  public InputStream locate(final String uri)
      throws IOException {
    notNull(uri, "URI cannot be NULL!");
    LOG.debug("locate resource: {}", uri);

    try {
      if (getWildcardStreamLocator().hasWildcard(uri)) {
        final ServletContext servletContext = Context.get().getServletContext();
        final String fullPath = FilenameUtils.getFullPath(uri);
        final String realPath = servletContext.getRealPath(fullPath);
        if (realPath == null) {
          final String message = "[FAIL] determine realPath for resource: " + uri;
          LOG.debug(message);
          throw new IOException(message);
        }
        return getWildcardStreamLocator().locateStream(uri, new File(URLDecoder.decode(realPath, StandardCharsets.UTF_8.name())));
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
      switch (locatorStrategy) {
        case DISPATCHER_FIRST:
          inputStream = dispatcherFirstStreamLocator(uri);
          break;
        case SERVLET_CONTEXT_FIRST:
          inputStream = servletContextFirstStreamLocator(uri);
          break;
        case SERVLET_CONTEXT_ONLY:
          inputStream = servletContextBasedStreamLocator(uri);
          break;
      }
      validateInputStreamIsNotNull(inputStream, uri);
      return inputStream;
    } catch (final IOException e) {
      LOG.debug("Wrong or empty resource with location: {}", uri);
      throw e;
    }
  }

  private InputStream servletContextFirstStreamLocator(final String uri)
      throws IOException {
    try {
      return servletContextBasedStreamLocator(uri);
    } catch (final Exception e) {
      LOG.debug("retrieving servletContext stream for uri: {}", uri);
      return locateWithDispatcher(uri);
    }
  }

  private InputStream dispatcherFirstStreamLocator(final String uri)
      throws IOException {
    try {
      return locateWithDispatcher(uri);
    } catch (final Exception e) {
      LOG.debug("retrieving servletContext stream for uri: {}", uri);
      return servletContextBasedStreamLocator(uri);
    }
  }

  /**
   * @VisibleForTesting
   */
  InputStream locateWithDispatcher(final String uri)
      throws IOException {
    final Context context = Context.get();
    if (dispatcherLocator == null) {
      LOG.error("DispatcherLocator was not injected properly. This is a BUG.");
      throw new IllegalStateException("DispatcherLocator not injected properly");
    }
    // The order of stream retrieval is important. We are trying to get the dispatcherStreamLocator in order to handle
    // jsp resources (if such exist). Switching the order would cause jsp to not be interpreted by the container.
    return dispatcherLocator.getInputStream(context.getRequest(), context.getResponse(), uri);
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

  /**
   * @return the strategy used by this locator.
   */
  public LocatorStrategy getLocatorStrategy() {
    return locatorStrategy;
  }

  @Override
  public String toString() {
    return super.toString() + "#" + locatorStrategy;
  }
}
