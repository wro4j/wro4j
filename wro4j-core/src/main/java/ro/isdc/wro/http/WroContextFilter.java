/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;


/**
 * This filter is responsible for setting the {@link Context} to the current request cycle. This is required if you want
 * to use {@link ServletContextAttributeHelper} in order to access wro related attributes from within a tag or a
 * servlet. Usually this filter will be mapped to all requests:
 * 
 * <pre>
 *  <filter-mapping>
 *     <filter-name>wroContextFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 *  </filter-mapping>
 * 
 * </pre>
 * 
 * @author Alex Objelean
 * @created 12 May 2012
 * @since 1.4.7
 */
public class WroContextFilter
  implements Filter {
  private FilterConfig filterConfig;

  /**
   * {@inheritDoc}
   */
  public void init(final FilterConfig filterConfig)
      throws ServletException {
    this.filterConfig = filterConfig;
  }
  
  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    final String oldCorrelationId = Context.isContextSet()?Context.getCorrelationId():null;
    Context.set(Context.webContext(request, response, this.filterConfig), getWroConfiguration());
    final String correlationId = Context.getCorrelationId();
    try {
      chain.doFilter(request, response);
    } finally {
      Context.setCorrelationId(correlationId);
      Context.unset();
      if(oldCorrelationId != null) Context.setCorrelationId(oldCorrelationId);
    }
  }

  /**
   * @return the {@link WroConfiguration} extracted from {@link ServletContext} if exist or default one otherwise. 
   */
  private WroConfiguration getWroConfiguration() {
    final WroConfiguration configAttribute = getServletContextAttributeHelper().getWroConfiguration();
    final WroConfiguration config = configAttribute != null ? configAttribute : new WroConfiguration();
    return config;
  }

  /**
   * @VisibleForTesting
   * @return the instance responsible for {@link WroConfiguration} lookup.
   */
  ServletContextAttributeHelper getServletContextAttributeHelper() {
    return ServletContextAttributeHelper.create(filterConfig);
  }
  
  /**
   * {@inheritDoc}
   */
  public void destroy() {
    Context.destroy();
  }
}
