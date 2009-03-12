/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.http;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.exception.WroRuntimeException;

/**
 * ContextHolder. Contains static reference to thread local properties holding
 * request, response & servletContext objets.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 27, 2008
 */
public final class ContextHolder {
  /**
   * Holds {@link HttpServletRequest} objects for current thread.
   */
  public static final ThreadLocal<HttpServletRequest> REQUEST_HOLDER = new ThreadLocal<HttpServletRequest>() {
    /**
     * Throws runtime exception if get method returns null reference.
     * {@inheritDoc}
     */
    @Override
    public HttpServletRequest get() {
      HttpServletRequest request = super.get();
      if (request == null) {
        throw new WroRuntimeException(
            "Could not find valid HttpServletRequest!");
      }
      return request;
    }
  };

  /**
   * Holds {@link HttpServletResponse} objects for current thread.
   */
  public static final ThreadLocal<HttpServletResponse> RESPONSE_HOLDER = new ThreadLocal<HttpServletResponse>() {
    /**
     * Throws runtime exception if get method returns null reference.
     * {@inheritDoc}
     */
    @Override
    public HttpServletResponse get() {
      HttpServletResponse response = super.get();
      if (response == null) {
        throw new WroRuntimeException(
            "Could not find valid HttpServletResponse!");
      }
      return response;
    }
  };

  /**
   * Holds {@link HttpServletResponse} objects for current thread.
   */
  public static final ThreadLocal<ServletContext> SERVLET_CONTEXT_HOLDER = new ThreadLocal<ServletContext>() {
    /**
     * Throws runtime exception if get method returns null reference.
     * {@inheritDoc}
     */
    @Override
    public ServletContext get() {
      ServletContext servletContext = super.get();
      if (servletContext == null) {
        throw new WroRuntimeException("Could not find valid ServletContext!");
      }
      return servletContext;
    }
  };

}
