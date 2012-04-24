/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The purpose of this locator is to dispatch the request to a given context relative location (doesn't have to include
 * the context path).
 * <p/>
 * Initial implementation used a request dispatcher, but since it doesn't behave correctly when the request are issued
 * outside of request cycle (cache reload scheduler), another approach is preferred. The new approach is to compute
 * absolute url of the request it as a separate connection using {@link UrlUriLocator}.
 * 
 * @author Alex Objelean
 */
public class DispatcherStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherStreamLocator.class);
  /**
   * Used to locate external resources. No wildcard handling is required.
   */
  private UriLocator externalUriLocator = newExternalUriLocator();
  
  /**
   * @param location
   *          a context relative location to retrieve stream for. This 
   * @return a valid stream for required location. This method will never return a null.
   * @throws IOException
   *           if the stream cannot be located at the specified location.
   */
  public InputStream getInputStream(final HttpServletRequest request, final HttpServletResponse response,
    final String location)
    throws IOException {
    Validate.notNull(request);
    Validate.notNull(response);

    final String absolutePath = computeServletContextPath(request) + location;
    LOG.debug("Locating resource: {}", absolutePath);
    return externalUriLocator.locate(absolutePath);
  }

  /**
   * @return the part URL from the protocol name up to the query string and contextPath.
   */
  private String computeServletContextPath(final HttpServletRequest request) {
    return request.getRequestURL().toString().replace(request.getServletPath(), "");
  }
  
  /**
   * @return {@link UriLocator} used to retrieve stream for absolute location .
   */
  protected UriLocator newExternalUriLocator() {
    return new UrlUriLocator().setEnableWildcards(false);
  }
}