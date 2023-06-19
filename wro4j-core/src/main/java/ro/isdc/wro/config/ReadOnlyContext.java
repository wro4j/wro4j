/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import java.lang.reflect.Proxy;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * A readonly version of {@link Context}. Prefer to use fields of this type for injection in processors, because it is
 * thread-safe (the getAggregatedFolderPath is computed correctly in multi-threaded environment).
 *
 * The interface is required to make it possible to create {@link Proxy} of the {@link Context} during injection using
 * standard jdk support. It is possible to create also proxies for classes, but a new dependency is required for this
 * (javassist), which is something I prefer to avoid at this point.
 * 
 * @author Alex Objelean
 */
public interface ReadOnlyContext {
  /**
   * @return {@link WroConfiguration} singleton instance.
   */
  public WroConfiguration getConfig();

  /**
   * @return the request
   */
  public HttpServletRequest getRequest();

  /**
   * @return the response
   */
  public HttpServletResponse getResponse();

  /**
   * @return the servletContext
   */
  public ServletContext getServletContext();

  /**
   * @return the filterConfig
   */
  public FilterConfig getFilterConfig();

  /**
   * This field is useful only for the aggregated resources of type {@link ResourceType#CSS}.<br/>The
   * aggregatedFolderPath is used to compute the depth. For example, if aggregatedFolder is "wro" then the depth is 1
   * and the path used to prefix the image url is <code>".."</code>. If the aggregatedFolder is "css/aggregated", the
   * depth is 2 and the prefix is <code>"../.."</code>. The name of the aggregated folder is not important, it is used
   * only to compute the depth.
   * 
   * @return the aggregatedFolderPath
   */
  public String getAggregatedFolderPath();
}
