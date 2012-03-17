package ro.isdc.wro.http.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Allows configuration of a list of processors to be applied on the    
 * 
 * @author Alex Objelean
 * @since 1.4.5
 * @created 17 Mar 2012
 */
public abstract class AbstractProcessorFilter
    implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(WroFilter.class);
  private FilterConfig filterConfig;
  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
    throws ServletException {
    this.filterConfig = config;
  }
  
  /**
   * {@inheritDoc}
   */
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    try {
      // add request, response & servletContext to thread local
      Context.set(Context.webContext(request, response, filterConfig));
      chain.doFilter(req, res);
    } catch (final RuntimeException e) {
      onRuntimeException(e, response, chain);
    } finally {
      Context.unset();
    }
  }
  
  /**
   * Invoked when a {@link RuntimeException} is thrown. Allows custom exception handling. The default implementation
   * redirects to 404 for a specific {@link WroRuntimeException} exception when in DEPLOYMENT mode.
   *
   * @param e {@link RuntimeException} thrown during request processing.
   */
  protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response,
    final FilterChain chain) {
    LOG.debug("RuntimeException occured", e);
    try {
      LOG.debug("Cannot process. Proceeding with chain execution.");
      final OutputStream os = new ByteArrayOutputStream();
      HttpServletResponse wrappedResponse = new RedirectedStreamServletResponseWrapper(os, response);
      chain.doFilter(Context.get().getRequest(), wrappedResponse);
    } catch (final Exception ex) {
      // should never happen
      LOG.error("Error while chaining the request: " + HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * @return a list of processors to apply for this filter.
   */
  protected abstract List<ResourcePreProcessor> processorsList();
}
