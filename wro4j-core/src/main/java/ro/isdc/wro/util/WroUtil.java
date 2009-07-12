/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.util;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.ContextHolder;

/**
 * Utility class.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 13, 2008
 */
public final class WroUtil {
  /**
   * Empty line pattern.
   */
  public static Pattern EMTPY_LINE_PATTERN = Pattern.compile(
      "^[\\t ]*$\\r?\\n", Pattern.MULTILINE);

  /**
   * Retrieve pathInfo from a given location.
   *
   * @param location
   *          where to search contextPath.
   * @return pathInfo value.
   */
  public static String getPathInfoFromLocation(final String location) {
    final String noSlash = location.substring(1);
    final int nextSlash = noSlash.indexOf('/');
    if (nextSlash == -1) {
      return "";
    }
    final String pathInfo = noSlash.substring(nextSlash);
    return pathInfo;
  }

  /**
   * @return contextPath associated with the request from the current thread.
   */
  public static String getContextPath() {
    final HttpServletRequest request = ContextHolder.REQUEST_HOLDER.get();
    if (request == null) {
      throw new IllegalStateException(
          "Could not locate Context Path from LocalThread!");
    }
    return request.getContextPath();
  }

  /**
   * @return requestUri associated with the request from the current thread.
   */
  public static String getRequestUri() {
    final HttpServletRequest request = ContextHolder.REQUEST_HOLDER.get();
    if (request == null) {
      throw new IllegalStateException(
          "Could not locate Context Path from LocalThread!");
    }
    return request.getRequestURI();
  }

  /**
   * @return the request uri path - the part until the last / character. For
   *         instance if request uri is: /app/wro/all.css => /app/wro/
   */
  public static String getRequestUriPath() {
    final String requestUri = getRequestUri();
    final int idxLastSeparator = requestUri.lastIndexOf('/');
    return requestUri.substring(0, idxLastSeparator + 1);
  }

  /**
   * Retrieve servletPath from a given location.
   *
   * @param location
   *          where to search the servletPath.
   * @return ServletPath string value.
   */
  public static String getServletPathFromLocation(final String location) {
    return location.replace(getPathInfoFromLocation(location), "");
  }

  /**
   * Adds the gzip HTTP header to the response. This is need when a gzipped body
   * is returned so that browsers can properly decompress it. <p/>
   *
   * @param response
   *          the response which will have a header added to it. I.e this method
   *          changes its parameter
   * @throws WroRuntimeException
   *           if response doesnt contains Content-Encoding header.
   */
  public static void addGzipHeader(final HttpServletResponse response) {
    response.setHeader("Content-Encoding", "gzip");
    final boolean containsEncoding = response.containsHeader("Content-Encoding");
    if (!containsEncoding) {
      throw new WroRuntimeException("Failure when attempting to set "
          + "Content-Encoding: gzip");
    }
  }
}
