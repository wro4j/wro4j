/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.WroProcessResult;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroUtil;

/**
 * WroFilter.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 31, 2008
 */
public class WroFilter implements Filter {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(WroFilter.class);

  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  public static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;

  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config) throws ServletException {
    this.filterConfig = config;
    this.wroManagerFactory = getWroManagerFactory();
    doInit(config);
  }

  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config) throws ServletException {}

  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req,
      final ServletResponse res, final FilterChain chain) throws IOException,
      ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    // add request, response & servletContext to thread local
    Context.set(new Context(request, response, filterConfig));
    final String requestURI = request.getRequestURI();
    final WroManager manager = wroManagerFactory.getInstance();

    //set response headers
    response.setHeader("Cache-Control", "max-age=3600");

    // probably would be better to test with startsWith(servletContext +
    // resources)
    if (requestURI.contains(CssUrlRewritingProcessor.PATH_RESOURCES)) {
      final String resourceId = request
          .getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
      final InputStream is = manager.getUriLocatorFactory().getInstance(
          resourceId).locate(resourceId);
      if (is == null) {
        throw new WroRuntimeException("Could not Locate resource: "
            + resourceId);
      }
      final OutputStream os = response.getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();
    } else {
      // process the uri using manager
      final WroProcessResult result = manager.process(requestURI);
      response.setContentType(result.getContentType());
      final InputStream is = result.getInputStream();

      OutputStream os = response.getOutputStream();
      if (Context.get().isGzipEnabled()) {
        os = getGzipedOutputStream(response);
      }
      // append result to response stream
      IOUtils.copy(is, os);
      is.close();
      os.close();
    }
    //remove context from the current thread local.
    Context.unset();
  }

  /**
   * Add gzip header to response and wrap the response {@link OutputStream} with
   * {@link GZIPOutputStream}
   *
   * @param response
   *          {@link HttpServletResponse} object.
   * @return wrapped gziped OutputStream.
   */
  private OutputStream getGzipedOutputStream(final HttpServletResponse response)
      throws IOException {
    // gzip response
    WroUtil.addGzipHeader(response);
    // Create a gzip stream
    final OutputStream os = new GZIPOutputStream(response.getOutputStream());
    log.debug("Gziping outputStream response");
    return os;
  }

  /**
   * Factory method for {@link WroManagerFactory}. Override this method, in
   * order to change the way filter use factory.
   *
   * @return {@link WroManagerFactory} object.
   */
  protected WroManagerFactory getWroManagerFactory() {
    final String appFactoryClassName = filterConfig
        .getInitParameter(PARAM_MANAGER_FACTORY);
    if (appFactoryClassName == null) {
      // If no context param was specified we return the default factory
      return new ServletContextAwareWroManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader()
            .loadClass(appFactoryClassName);
        // Instantiate the factory
        return (WroManagerFactory) factoryClass.newInstance();
      } catch (final ClassNotFoundException e) {
        throw new RuntimeException("", e);
      } catch (final InstantiationException e) {
        throw new RuntimeException("", e);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }

}
