/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
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
  private WroConfiguration wroConfig;
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
  public WroConfiguration getConfig() {
    return wroConfig;
  }


  /**
   * DO NOT CALL THIS METHOD UNLESS YOU ARE SURE WHAT YOU ARE DOING.
   * <p/>
   * sets the {@link WroConfiguration} singleton instance.
   */
  public void setConfig(final WroConfiguration config) {
    wroConfig = config;
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
   * @return {@link Context} associated with CURRENT request cycle.
   */
  public static Context get() {
    validateContext();
    return CURRENT.get();
  }

  /**
   * Checks if the {@link Context} is accessible from current request cycle.
   */
  private static void validateContext() {
    if (CURRENT.get() == null) {
      throw new WroRuntimeException("No context associated with CURRENT request cycle!");
    }
  }

  public static void set(final Context context) {
    set(context, new WroConfiguration());
  }

  /**
   * Associate a context with the CURRENT request cycle.
   *
   * @param context {@link Context} to set.
   */
  public static void set(final Context context, final WroConfiguration config) {
    Validate.notNull(context);
    Validate.notNull(config);
    CURRENT.set(context);
    CURRENT.get().setConfig(config);
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
   * Perform context clean-up.
   */
  public static void destroy() {
    unset();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
