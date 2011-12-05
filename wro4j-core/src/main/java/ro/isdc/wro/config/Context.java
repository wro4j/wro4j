/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.FieldsSavingRequestWrapper;
import ro.isdc.wro.model.resource.ResourceType;


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
  private transient HttpServletRequest request;
  /**
   * Response.
   */
  private transient HttpServletResponse response;
  /**
   * ServletContext.
   */
  private transient ServletContext servletContext;
  /**
   * FilterConfig.
   */
  private transient FilterConfig filterConfig;
  /**
   * The path to the folder, relative to the root, used to compute rewritten image url.
   */
  private String aggregatedFolderPath;


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
   * @return true if the call is done during wro4j request cycle. In other words, if the context is set.
   */
  public static boolean isContextSet() {
    return CURRENT.get() != null;
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
  private Context() {}


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
   * @return the aggregatedFolderPath
   */
  public String getAggregatedFolderPath() {
    return this.aggregatedFolderPath;
  }

  /**
   * This field is useful only for the aggregated resources of type {@link ResourceType#CSS}. </br>The
   * aggregatedFolderPath is used to compute the depth. For example, if aggregatedFolder is "wro" then the depth is 1
   * and the path used to prefix the image url is <code>".."</code>. If the aggregatedFolder is "css/aggregated", the
   * depth is 2 and the prefix is <code>"../.."</code>. The name of the aggregated folder is not important, it is used
   * only to compute the depth.
   *
   * @param aggregatedFolderPath the aggregatedFolderPath to set
   */
  public void setAggregatedFolderPath(final String aggregatedFolderPath) {
    this.aggregatedFolderPath = aggregatedFolderPath;
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
