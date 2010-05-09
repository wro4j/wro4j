/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DynamicStreamLocatorStrategy. Defines the way a inputStream is located using different types of streams after
 * dispatching the request to provided location.
 *
 * @author Alex Objelean
 */
interface DynamicStreamLocatorStrategy {
  /**
   * @param request {@link HttpServletRequest} object.
   * @param response {@link HttpServletResponse} object.
   * @param location where to dispatch.
   * @return InputStream of the dispatched resource.
   * @throws IOException if an input or output exception occurred
   */
  InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
    final String location)
    throws IOException;
}