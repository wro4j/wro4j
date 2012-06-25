/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * Holds the properties related to a request cycle.
 *
 * @author Alex Objelean
 */
public interface Context {
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
   * @return the aggregatedFolderPath
   */
  public String getAggregatedFolderPath();
}
