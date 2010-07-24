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

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.FieldsSavingRequestWrapper;


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
   * {@link WroConfiguration} is not stored inside a {@link ThreadLocal}, because it must be accessible outside of
   * request cycle.
   */
  private static WroConfiguration CONFIG_INSTANCE = new WroConfiguration();
  /**
   * Request.
   */
  private HttpServletRequest request;
  /**
   * Response.
   */
  private HttpServletResponse response;
  /**
   * ServletContext.
   */
  private ServletContext servletContext;
  /**
   * FilterConfig.
   */
  private FilterConfig filterConfig;

  /**
   * @return {@link WroConfiguration} singleton instance.
   */
  public static WroConfiguration getConfig() {
    return CONFIG_INSTANCE;
  }


  /**
   * DO NOT CALL THIS METHOD UNLESS YOU ARE SURE WHAT YOU ARE DOING.
   * <p/>
   * sets the {@link WroConfiguration} singleton instance.
   */
  public static void setConfig(final WroConfiguration config) {
    CONFIG_INSTANCE = config;
  }


  /**
   * A context useful for running in web context (inside a servlet container).
   */
  public static Context webContext(final HttpServletRequest request, final HttpServletResponse response,
    final FilterConfig filterConfig) {
    return new Context(request, response, filterConfig);
  }


  /**
   * A context useful for running in non web context (standAlone applications).
   */
  public static Context standaloneContext() {
    return new Context();
  }


  /**
   * Creates a Context which knows only about {@link HttpServletRequest} object.
   *
   * @param request {@link HttpServletRequest} for this context.
   * @return {@link Context} instance.
   */
  public static Context standaloneContext(final HttpServletRequest request) {
    return new Context(request, null, null);
  }


  /**
   * @return {@link Context} associated with CURRENT request cycle.
   */
  public static Context get() {
    if (!isValid()) {
      throw new WroRuntimeException("No context associated with CURRENT request cycle!");
    }
    return CURRENT.get();
  }

  /**
   * @return true if the Context is valid (not null), meaning that it is inside a request cycle.
   */
  private static boolean isValid() {
    return CURRENT.get() != null;
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
  }


  /**
   * Constructor.
   */
  private Context(final HttpServletRequest request, final HttpServletResponse response, final FilterConfig filterConfig) {
    this.request = new FieldsSavingRequestWrapper(request);

    this.response = response;
    if (filterConfig != null) {
      this.servletContext = filterConfig.getServletContext();
    } else {
      this.servletContext = null;
    }
    this.filterConfig = filterConfig;
  }

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return this.request;
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
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
