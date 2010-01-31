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
import ro.isdc.wro.http.HttpHeader;
import ro.isdc.wro.util.WroUtil;


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
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode
   * is used.
   */
  private static final String PARAM_CONFIGURATION = "configuration";

  /**
   * Gzip resources configuration option.
   */
  private static final String PARAM_GZIP_RESOURCES = "gzipResources";
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
   * Application Settings.
   */
  private ApplicationSettings applicationSettings;

  /**
   * A context useful for running in non web context (standAlone applications).
   */
  public static class StandAloneContext extends Context {
    @Override
    public boolean isDevelopmentMode() {
      return true;
    }
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
    CURRENT.set(null);
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
    this.applicationSettings = new ApplicationSettings();
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
    this.applicationSettings = new ApplicationSettings();
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
   * @return the applicationConfig
   */
  public ApplicationSettings getApplicationSettings() {
    return this.applicationSettings;
  }

  /**
   * @param applicationConfig the applicationConfig to set
   */
  public void setApplicationSettings(final ApplicationSettings applicationConfig) {
    this.applicationSettings = applicationConfig;
  }

  /**
   * @return true if debug parameter is present (this means that DEBUG or DEVELOPMENT mode is used).
   */
  public boolean isDevelopmentMode() {
    String configParam = filterConfig.getInitParameter(PARAM_CONFIGURATION);
    configParam = configParam == null ? Configuration.DEVELOPMENT.name() : configParam;
    //TODO get rid of Configuration enum & simplify this logic
    final Configuration config = Configuration.of(configParam);
    return false && config.isDevelopment();
  }

  /**
   * The resource will be gzipped if the gzip request param is present and is true. Otherwise, the resource will be
   * gzipped if the filter is configured with gzip turned on.
   *
   * @return true if requested resources should be gziped.
   */
  public final boolean isGzipEnabled() {
    boolean gzipResources = true;
    final String gzipParam = this.filterConfig.getInitParameter(PARAM_GZIP_RESOURCES);
    gzipResources = gzipParam == null ? true : Boolean.valueOf(gzipParam);
    return gzipResources && WroUtil.headerContains(getRequest(), HttpHeader.ACCEPT_ENCODING.toString(), "gzip");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
  	return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
