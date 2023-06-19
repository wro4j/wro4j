/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.http.WroFilter;


/**
 * Holds the properties related to a request cycle.
 *
 * @author Alex Objelean
 */
public class Context
    implements ReadOnlyContext {
  /**
   * Maps correlationId with a Context.
   */
  private static final Map<String, Context> CONTEXT_MAP = Collections.synchronizedMap(new HashMap<String, Context>());
  /**
   * Holds a correlationId, created in {@link WroFilter}. A correlationId will be associated with a {@link Context}
   * object.
   */
  private static ThreadLocal<String> CORRELATION_ID = new ThreadLocal<String>();
  private WroConfiguration config;
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
    return config;
  }


  /**
   * <p>DO NOT CALL THIS METHOD UNLESS YOU ARE SURE WHAT YOU ARE DOING.</p>
   *
   * <p>Sets the {@link WroConfiguration} singleton instance.</p>
   */
  public void setConfig(final WroConfiguration config) {
    notNull(config);
    this.config = config;
  }


  /**
   * A context useful for running in web context (inside a servlet container).
   */
  public static Context webContext(final HttpServletRequest request, final HttpServletResponse response,
    final FilterConfig filterConfig) {
    return new Context(request, response, filterConfig);
  }


  /**
   * A context useful for running in non web context (standalone applications).
   */
  public static Context standaloneContext() {
    return new Context();
  }


  /**
   * @return {@link Context} associated with CURRENT request cycle.
   */
  public static Context get() {
    validateContext();
    final String correlationId = CORRELATION_ID.get();
    return CONTEXT_MAP.get(correlationId);
  }

  /**
   * @return true if the call is done during wro4j request cycle. In other words, if the context is set.
   */
  public static boolean isContextSet() {
    return CORRELATION_ID.get() != null && CONTEXT_MAP.get(CORRELATION_ID.get()) != null;
  }

  /**
   * Checks if the {@link Context} is accessible from current request cycle.
   */
  private static void validateContext() {
    if (!isContextSet()) {
      throw new WroRuntimeException("No context associated with CURRENT request cycle!");
    }
  }

  /**
   * Set a context with default configuration to current thread.
   */
  public static void set(final Context context) {
    set(context, new WroConfiguration());
  }

  /**
   * Associate a context with the CURRENT request cycle.
   *
   * @param context {@link Context} to set.
   */
  public static void set(final Context context, final WroConfiguration config) {
    notNull(context);
    notNull(config);
    context.setConfig(config);

    final String correlationId = generateCorrelationId();
    CORRELATION_ID.set(correlationId);
    CONTEXT_MAP.put(correlationId, context);
  }

  /**
   * @return a string representation of an unique id used to store Context in a map.
   */
  private static String generateCorrelationId() {
    return UUID.randomUUID().toString();
  }


  /**
   * Remove context from the local thread.
   */
  public static void unset() {
    final String correlationId = CORRELATION_ID.get();
    if (correlationId != null) {
      CONTEXT_MAP.remove(correlationId);
    }
    CORRELATION_ID.remove();
  }


  /**
   * Perform context clean-up.
   */
  public static void destroy() {
    unset();
    //remove all context objects stored in map
    CONTEXT_MAP.clear();
  }

  /**
   * Set the correlationId to the current thread.
   */
  public static void setCorrelationId(final String correlationId) {
    notNull(correlationId);
    CORRELATION_ID.set(correlationId);
  }

  /**
   * <p>Remove the correlationId from the current thread. This operation will not remove the {@link Context} associated
   * with the correlationId. In order to remove context, call {@link Context#unset()}.</p>
   *
   * <p>Unsetting correlationId is useful when you create child threads which needs to access the correlationId from the
   * parent thread. This simulates the {@link InheritableThreadLocal} functionality.</p>
   */
  public static void unsetCorrelationId() {
    CORRELATION_ID.remove();
  }

  /**
   * @return the correlationId associated with this thread.
   */
  public static String getCorrelationId() {
    validateContext();
    return CORRELATION_ID.get();
  }

  /**
   * Decorates a callable with {@link ContextPropagatingCallable} making it possible to access the {@link Context} from
   * within the decorated callable.
   *
   * @param callable
   *          the {@link Callable} to decorate.
   * @return the decorated callable.
   */
  public static <T> Callable<T> decorate(final Callable<T> callable) {
    return new ContextPropagatingCallable<T>(callable);
  }

  /**
   * @return number of {@link Context} objects which were created but not yet destroyed. This is useful to detect leaks
   *         (mostly for unit testing).
   */
  public static int countActive() {
    return CONTEXT_MAP.size();
  }

  /**
   * Private constructor. Used to build {@link StandAloneContext}.
   */
  private Context() {}


  /**
   * Constructor.
   */
  private Context(final HttpServletRequest request, final HttpServletResponse response, final FilterConfig filterConfig) {
    notNull(request);
    notNull(response);
    this.request = request;
    this.response = response;
    this.servletContext = filterConfig != null ? filterConfig.getServletContext() : null;
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

  public String getAggregatedFolderPath() {
    return this.aggregatedFolderPath;
  }

  /**
   * @param aggregatedFolderPath the aggregatedFolderPath to set
   */
  public void setAggregatedFolderPath(final String aggregatedFolderPath) {
    this.aggregatedFolderPath = aggregatedFolderPath;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
