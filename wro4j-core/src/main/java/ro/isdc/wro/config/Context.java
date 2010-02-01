/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ro.isdc.wro.exception.WroRuntimeException;


/**
 * Holds the properties related to a request cycle.
 *
 * @author Alex Objelean
 */
public class Context {
  /**
   * Thread local holding CURRENT context. This instance is of {@link InheritableThreadLocal} type because threads
   * created by this thread should be able to access the {@link Context}.
   */
  private static final ThreadLocal<Context> CURRENT = new InheritableThreadLocal<Context>();
  /**
   * Request.
   */
  private final HttpServletRequest request;
  /**
   * The uri of the request. This is used because the inherited thread local doesn't preserve
   */
  private final String requestURI;
  /**
   * Response.
   */
  private final HttpServletResponse response;
  /**
   * ServletContext.
   */
  private final ServletContext servletContext;
  /**
   * FilterConfig.
   */
  private final FilterConfig filterConfig;

  /**
   * A context useful for running in non web context (standAlone applications).
   */
  public static class StandAloneContext extends Context {
  }

  /**
   * @return {@link Context} associated with CURRENT request cycle.
   */
  public static Context get() {
    final Context context = CURRENT.get();
    if (context == null) {
      throw new WroRuntimeException("No context associated with CURRENT request cycle!");
    }
    return context;
  }

  /**
   * Associate a context with the CURRENT request cycle.
   *
   * @param context {@link Context} to set.
   */
  public static void set(final Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be NULL!");
    }
    CURRENT.set(context);
  }

  /**
   * Remove context from the local thread.
   */
  public static void unset() {
    CURRENT.remove();
  }

  /**
   * Private constructor. Used to build {@link StandAloneContext}.
   */
  private Context() {
    this.request = null;
    this.response = null;
    this.servletContext = null;
    this.filterConfig = null;
    this.requestURI = null;
  }

  /**
   * Constructor.
   */
  public Context(final HttpServletRequest request, final HttpServletResponse response, final FilterConfig filterConfig) {
    this.request = request;
    this.requestURI = request.getRequestURI();
    this.response = response;
    this.servletContext = filterConfig.getServletContext();
    this.filterConfig = filterConfig;
  }

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return this.request;
  }

  /**
	 * @return the requestURI
	 */
	public String getRequestURI() {
		return requestURI;
	}

  /**
   * @return the response
   */
  public HttpServletResponse getResponse() {
    return this.response;
  }

  /**
   * @return the servletContext
   */
  public ServletContext getServletContext() {
    return this.servletContext;
  }

  /**
   * @return the filterConfig
   */
  public FilterConfig getFilterConfig() {
    return this.filterConfig;
  }

  /**
   * @return true if debug parameter is present (this means that DEBUG or DEVELOPMENT mode is used).
   */
  public boolean isDevelopmentMode1() {
//    String configParam = filterConfig.getInitParameter(PARAM_CONFIGURATION);
//    configParam = configParam == null ? Configuration.DEVELOPMENT.name() : configParam;
//    //TODO get rid of Configuration enum & simplify this logic
//    final Configuration config = Configuration.of(configParam);
//    return false && config.isDevelopment();
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
  	return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
