/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Utility class.
 *
 * @author Alex Objelean
 * @created Created on Nov 13, 2008
 */
public final class WroUtil {
  private static final Logger LOG = LoggerFactory.getLogger(WroUtil.class);
  /**
   * Empty line pattern.
   */
  public static final Pattern EMTPY_LINE_PATTERN = Pattern.compile("^[\\t ]*$\\r?\\n", Pattern.MULTILINE);
  /**
   * Thread safe date format used to transform milliseconds into date as string to put in response header.
   */
  private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("E, dd MMM yyyy HH:mm:ss z",
    TimeZone.getTimeZone("GMT"));
  /**
   * Patterns used to search for mangled Accept-Encoding header.
   */
  private static final Pattern PATTERN_ACCEPT_ENCODING = Pattern.compile(
    "(?im)^(Accept-Encoding|Accept-EncodXng|X-cept-Encoding|X{15}|~{15}|-{15})$");
  private static final Pattern PATTERN_GZIP = Pattern.compile(
    "(?im)^((gzip|deflate)\\s?,?\\s?(gzip|deflate)?.*|X{4,13}|~{4,13}|-{4,13})$");

  private static final AtomicInteger threadFactoryNumber = new AtomicInteger(1);

  /**
   * @return {@link ThreadFactory} with daemon threads.
   */
  public static ThreadFactory createDaemonThreadFactory(final String name) {
    return new ThreadFactory() {
      private final String prefix = "wro4j-" + name + "-" + threadFactoryNumber.getAndIncrement() + "-thread-";
      private final AtomicInteger threadNumber = new AtomicInteger(1);

      public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable, prefix + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
      }
    };
  }

  /**
   * Transforms milliseconds into date format for response header of this form: Sat, 10 Apr 2010 17:31:31 GMT.
   *
   * @param milliseconds to transform
   * @return string representation of the date.
   */
  public static String toDateAsString(final long milliseconds) {
    return DATE_FORMAT.format(milliseconds);
  }


  /**
   * Retrieve pathInfo from a given location.
   *
   *
   * @param request
   * @param location where to search contextPath.
   * @return pathInfo value.
   */
  public static String getPathInfoFromLocation(final HttpServletRequest request, final String location) {
    if (StringUtils.isEmpty(location)) {
      throw new IllegalArgumentException("Location cannot be empty string!");
    }
    final String contextPath = request.getContextPath();
    if (contextPath != null) {
      if (startsWithIgnoreCase(location, contextPath)) {
        return location.substring(contextPath.length());
      } else {
        return location;
      }
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
   * <p>
   * Case insensitive check if a String starts with a specified prefix.
   * </p>
   * <p>
   * <code>null</code>s are handled without exceptions. Two <code>null</code> references are considered to be equal. The
   * comparison is case insensitive.
   * </p>
   *
   * <pre>
   * StringUtils.startsWithIgnoreCase(null, null)      = true
   * StringUtils.startsWithIgnoreCase(null, "abcdef")  = false
   * StringUtils.startsWithIgnoreCase("abc", null)     = false
   * StringUtils.startsWithIgnoreCase("abc", "abcdef") = true
   * StringUtils.startsWithIgnoreCase("abc", "ABCDEF") = true
   * </pre>
   *
   * @see java.lang.String#startsWith(String)
   * @param str the String to check, may be null
   * @param prefix the prefix to find, may be null
   * @return <code>true</code> if the String starts with the prefix, case insensitive, or both <code>null</code>
   * @since 2.4
   */
  public static boolean startsWithIgnoreCase(final String str, final String prefix) {
    return startsWith(str, prefix, true);
  }

  /**
   * Creates a folder like implementation for a class. Ex: com.mycompany.MyClass -> com/mycompany/
   *
   * @param clazz
   *          used as a base location for determining the package path.
   * @return a string representation of the path where the class resides.
   */
  public static String toPackageAsFolder(final Class<?> clazz) {
    Validate.notNull(clazz, "Class cannot be null!");
    return clazz.getPackage().getName().replace('.', '/');
  }

  /**
   * <p>
   * Check if a String starts with a specified prefix (optionally case insensitive).
   * </p>
   *
   * @see java.lang.String#startsWith(String)
   * @param str the String to check, may be null
   * @param prefix the prefix to find, may be null
   * @param ignoreCase inidicates whether the compare should ignore case (case insensitive) or not.
   * @return <code>true</code> if the String starts with the prefix or both <code>null</code>
   */
  private static boolean startsWith(final String str, final String prefix, final boolean ignoreCase) {
    if (str == null || prefix == null) {
      return (str == null && prefix == null);
    }
    if (prefix.length() > str.length()) {
      return false;
    }
    return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
  }


  /**
   * Retrieve servletPath from a given location.
   *
   * @param location where to search the servletPath.
   * @return ServletPath string value.
   */
  public static String getServletPathFromLocation(final HttpServletRequest request, final String location) {
    return location.replace(getPathInfoFromLocation(request, location), "");
  }

  /**
   * Analyze headers of the request and searches for mangled (by proxy) for "Accept-Encoding" header and its mangled
   * variations and gzip header value and its mangled variations.
   *
   * @return true if this request support gzip encoding.
   */
  @SuppressWarnings("unchecked")
  public static boolean isGzipSupported(final HttpServletRequest request) {
    if (request != null) {
      final Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames != null) {
        while (headerNames.hasMoreElements()) {
          final String headerName = headerNames.nextElement();
          final Matcher m = PATTERN_ACCEPT_ENCODING.matcher(headerName);
          if (m.find()) {
            final String headerValue = request.getHeader(headerName);
            final Matcher mValue = PATTERN_GZIP.matcher(headerValue);
            return mValue.find();
          }
        }
      }
    }
    return false;
  }

  /**
   * Transforms a java multi-line string into javascript multi-line string.
   * This technique was found at {@link http://stackoverflow.com/questions/805107/multiline-strings-in-javascript/}
   * @param data a string containing new lines.
   * @return a string which being evaluated on the client-side will be treated as a correct multi-line string.
   */
  public static String toJSMultiLineString(final String data) {
    final String[] lines = data.split("\n");
    final StringBuffer result = new StringBuffer("[");
    if (lines.length == 0) {
      result.append("\"\"");
    }
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      result.append("\"");
      result.append(line.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\r|\\n", ""));
      //this is used to force a single line to have at least one new line (otherwise cssLint fails).
      if (lines.length == 1) {
        result.append("\\n");
      }
      result.append("\"");
      if (i < lines.length - 1) {
        result.append(",");
      }
    }
    result.append("].join(\"\\n\")");
    return result.toString();
  }

  /**
   * Add no-cache headers to response.
   */
  public static void addNoCacheHeaders(final HttpServletResponse response) {
    response.setHeader(HttpHeader.PRAGMA.toString(), "no-cache");
    response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "no-cache");
    response.setDateHeader(HttpHeader.EXPIRES.toString(), 0);
  }
  
  /**
   * Utility used to verify that requestURI matches provided path
   */
  public static boolean matchesUrl(HttpServletRequest request, final String path) {
    final Pattern pattern = Pattern.compile(".*" + path + "[/]?", Pattern.CASE_INSENSITIVE);
    if (request.getRequestURI() != null) {
      final Matcher m = pattern.matcher(request.getRequestURI());
      return m.matches();
    }
    return false;
  }


  /**
   * A factory method for creating a {@link ResourceProcessor} based on provided {@link ResourcePreProcessor}.
   * @param preProcessor {@link ResourcePreProcessor} to use as a {@link ResourceProcessor}.
   * @return instance of {@link ResourceProcessor}.
   */
  public static ResourcePostProcessor newResourceProcessor(final Resource resource, final ResourcePreProcessor preProcessor) {
    return new ResourcePostProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        preProcessor.process(resource, reader, writer);
      }
    };
  }


  /**
   * A simple way to create a {@link WroModelFactory}.
   *
   * @param model {@link WroModel} instance to be returned by the factory.
   */
  public static WroModelFactory factoryFor(final WroModel model) {
    return new WroModelFactory() {
      public WroModel create() {
        return model;
      }
      public void destroy() {}
    };
  }

  public static <T> ObjectFactory<T> simpleObjectFactory(final T object) {
    return new ObjectFactory<T>() {
      public T create() {
        return object;
      }
    };
  }


  /**
   * Wraps original exception into {@link WroRuntimeException} and throw it.
   *
   * @param e the exception to wrap.
   */
  public static void wrapWithWroRuntimeException(final Exception e) {
    LOG.error("Exception occured: " + e.getClass(), e.getCause());
    if (e instanceof WroRuntimeException) {
      throw (WroRuntimeException) e;
    }
    throw new WroRuntimeException(e.getMessage(), e);
  }
}
