/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.HttpHeader;


/**
 * Utility class.
 *
 * @author Alex Objelean
 * @created Created on Nov 13, 2008
 */
public final class WroUtil {
  /**
   * Empty line pattern.
   */
  public static Pattern EMTPY_LINE_PATTERN = Pattern.compile("^[\\t ]*$\\r?\\n", Pattern.MULTILINE);


  /**
   * Retrieve pathInfo from a given location.
   *
   * @param location where to search contextPath.
   * @return pathInfo value.
   */
  public static String getPathInfoFromLocation(final String location) {
    if (StringUtils.isEmpty(location)) {
      throw new IllegalArgumentException("Location cannot be empty string!");
    }
    final String noSlash = location.substring(1);
    final int nextSlash = noSlash.indexOf('/');
    if (nextSlash == -1) {
      return "";
    }
    final String pathInfo = noSlash.substring(nextSlash);
    return pathInfo;
  }


  /**
   * @return folder path for some uri - the part until the last / character. For instance if request uri is:
   *         /app/wro/all.css => /app/wro/
   */
  public static String getFolderOfUri(final String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("uri cannot be null!");
    }
    final int idxLastSeparator = uri.lastIndexOf('/');
    return uri.substring(0, idxLastSeparator + 1);
  }


  /**
   * Retrieve servletPath from a given location.
   *
   * @param location where to search the servletPath.
   * @return ServletPath string value.
   */
  public static String getServletPathFromLocation(final String location) {
    return location.replace(getPathInfoFromLocation(location), "");
  }


  /**
   * Adds the gzip HTTP header to the response. This is need when a gzipped body is returned so that browsers can
   * properly decompress it.
   * <p/>
   *
   * @param response the response which will have a header added to it. I.e this method changes its parameter
   * @throws WroRuntimeException if response doesnt contains Content-Encoding header.
   */
  public static void addGzipHeader(final HttpServletResponse response) {
    response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
    final boolean containsEncoding = response.containsHeader(HttpHeader.CONTENT_ENCODING.toString());
    if (!containsEncoding) {
      throw new WroRuntimeException("Failure when attempting to set " + "Content-Encoding: gzip");
    }
  }


  /**
   * @param request {@link HttpServletRequest} object.
   * @return true if this request support gzip encoding.
   */
  public static boolean isGzipSupported(final HttpServletRequest request) {
    return headerContains(request, HttpHeader.ACCEPT_ENCODING.toString(), "gzip");
  }


  /**
   * Checks if request contains the header value with a given value.
   *
   * @param request to check
   * @param header name of the header to check
   * @param value of the header to check
   */
  @SuppressWarnings("unchecked")
  public static boolean headerContains(final HttpServletRequest request, final String header, final String value) {
    final Enumeration<String> accepted = request.getHeaders(header);
    if (accepted != null) {
      while (accepted.hasMoreElements()) {
        final String headerValue = accepted.nextElement();
        if (headerValue.indexOf(value) != -1) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Normalize the path by removing occurrences of "..".
   *
   * @param path to normalize.
   * @return path string with double dots removed
   */
  public static String normalizePath(final String path) {
    final List<String> newcomponents = new ArrayList<String>(Arrays.asList(path.split("/")));
    for (int i = 0; i < newcomponents.size(); i++) {
      if (i < newcomponents.size() - 1) {
        // Verify for a ".." component at next iteration
        if ((newcomponents.get(i)).length() > 0 && newcomponents.get(i + 1).equals("..")) {
          newcomponents.remove(i);
          newcomponents.remove(i);
          i = i - 2;
          if (i < -1) {
            i = -1;
          }
        }
      }
    }
    final String newpath = join("/", newcomponents.toArray(new String[0]));
    if (path.endsWith("/")) {
      return newpath + "/";
    }
    return newpath;
  }


  /**
   * Joins string fragments using the specified separator
   *
   * @param separator
   * @param fragments
   * @return combined fragments
   */
  static String join(final String separator, final String... fragments) {
    if (fragments.length < 1) {
      // no elements
      return "";
    } else if (fragments.length < 2) {
      // single element
      return fragments[0];
    } else {
      // two or more elements
      final StringBuffer buff = new StringBuffer(128);
      if (fragments[0] != null) {
        buff.append(fragments[0]);
      }
      for (int i = 1; i < fragments.length; i++) {
        if ((fragments[i - 1] != null) || (fragments[i] != null)) {
          final boolean lhsClosed = fragments[i - 1].endsWith(separator);
          final boolean rhsClosed = fragments[i].startsWith(separator);
          if (lhsClosed && rhsClosed) {
            buff.append(fragments[i].substring(1));
          } else if (!lhsClosed && !rhsClosed) {
            buff.append(separator).append(fragments[i]);
          } else {
            buff.append(fragments[i]);
          }
        }
      }
      return buff.toString();
    }
  }
}
