/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.isdc.wro.http.HttpHeader;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


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
  public static Pattern EMTPY_LINE_PATTERN = Pattern.compile("^[\\t ]*$\\r?\\n", Pattern.MULTILINE);
  /**
   * Thread safe date format used to transform milliseconds into date as string to put in response header.
   */
  private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("E, dd MMM yyyy HH:mm:ss z",
    TimeZone.getTimeZone("GMT"));


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
    if (clazz == null) {
      throw new IllegalArgumentException("Class cannot be null!");
    }
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
  public static String getServletPathFromLocation(final String location) {
    return location.replace(getPathInfoFromLocation(location), "");
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
  private static boolean headerContains(final HttpServletRequest request, final String header, final String value) {
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
   * @return a {@link ThreadFactory} which produces only daemon threads.
   */
  public static ThreadFactory createDaemonThreadFactory() {
    final ThreadFactory backingThreadFactory = Executors.defaultThreadFactory();
    return new ThreadFactory() {
      public Thread newThread(final Runnable runnable) {
        final Thread thread = backingThreadFactory.newThread(runnable);
        thread.setDaemon(true);
        return thread;
      }
    };
  }


  /**
   * Returns the filter path read from the web.xml
   *
   * @param filterName the name of the searched filter.
   * @param is Stream of the web.xml file.
   * @return the filterPath for the searched filterName with wildcard removed.
   */
  public static String getFilterPath(final String filterName, final InputStream is)
    throws ServletException {
    if (filterName == null) {
      throw new IllegalArgumentException("filterName cannot be null!");
    }
    if (is == null) {
      throw new IllegalArgumentException("InputStream cannot be null!");
    }
    final String prefix = "filter";
    final String mapping = prefix + "-mapping";
  /**
   * Returns the filter path read from the web.xml
   *
   * @param filterName the name of the searched filter.
   * @param is Stream of the web.xml file.
   * @return
   */
//  public static String getFilterPath(final String filterName, final InputStream is)
//    throws ServletException {
//    final String prefix = "filter";
//    final String mapping = prefix + "-mapping";
//    final String name = prefix + "-name";
//
//    // Filter mappings look like this:
//    //
//    // <filter-mapping> <filter-name>WicketFilter</filter-name>
//    // <url-pattern>/*</url-pattern> <...> <filter-mapping>
//    try {
//      final ArrayList<String> urlPatterns = new ArrayList<String>();
//
//      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      factory.setNamespaceAware(true);
//      final Document document = factory.newDocumentBuilder().parse(is);
//      document.getDocumentElement().normalize();
//      final NodeList groupNodeList = document.getElementsByTagName(name);
//
//      final XmlPullParser parser = new XmlPullParser();
//      parser.parse(is);
//
//      while (true) {
//        XmlTag elem;
//        do {
//          elem = (XmlTag)parser.nextTag();
//        } while (elem != null && (!(elem.getName().equals(mapping) && elem.isOpen())));
//
//        if (elem == null) {
//          break;
//        }
//
//        String encounteredFilterName = null, urlPattern = null;
//
//        do {
//          elem = (XmlTag)parser.nextTag();
//          if (elem.isOpen()) {
//            parser.setPositionMarker();
//          } else if (elem.isClose() && elem.getName().equals(name)) {
//            encounteredFilterName = parser.getInputFromPositionMarker(elem.getPos()).toString().trim();
//          } else if (elem.isClose() && elem.getName().equals("url-pattern")) {
//            urlPattern = parser.getInputFromPositionMarker(elem.getPos()).toString().trim();
//          }
//        } while (urlPattern == null || encounteredFilterName == null);
//
//        if (filterName.equals(encounteredFilterName)) {
//          urlPatterns.add(urlPattern);
//        }
//      }
//
//      final String prefixUppered = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
//
//      // By the time we get here, we have a list of urlPatterns we match
//      // this filter against.
//      // In all likelihood, we will only have one. If we have none, we
//      // have an error.
//      // If we have more than one, we pick the first one to use for any
//      // 302 redirects that require absolute URLs.
//      if (urlPatterns.size() == 0) {
//        throw new IllegalArgumentException("Error initializing Wicket" + prefixUppered + " - you have no <" + mapping
//          + "> element with a url-pattern that uses " + prefix + ": " + filterName);
//      }
//      final String urlPattern = urlPatterns.get(0);
//
//      // Check for leading '/' and trailing '*'.
//      if (!urlPattern.startsWith("/") || !urlPattern.endsWith("*")) {
//        throw new IllegalArgumentException("<" + mapping + "> for Wicket" + prefixUppered + " \"" + filterName
//          + "\" must start with '/' and end with '*'.");
//      }
//
//      // Strip trailing '*' and keep leading '/'.
//      return stripWildcard(urlPattern);
//    } catch (final IOException e) {
//      throw new ServletException("Error finding <" + prefix + "> " + filterName + " in web.xml", e);
//    } catch (final ParseException e) {
//      throw new ServletException("Error finding <" + prefix + "> " + filterName + " in web.xml", e);
//    }
//  }

    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      final Document document = factory.newDocumentBuilder().parse(is);
      document.getDocumentElement().normalize();

      final XPathFactory xPathFactory = XPathFactory.newInstance();
      final XPath xpath = xPathFactory.newXPath();
      final XPathExpression filterNameExpression = xpath.compile("//filter-mapping/filter-name/text()");
      final XPathExpression urlPatternExpression = xpath.compile("url-pattern/text()");

      final Object result = filterNameExpression.evaluate(document, XPathConstants.NODESET);

      String urlPattern = null;

      final NodeList nodes = (NodeList) result;
      for (int i = 0; i < nodes.getLength(); i++) {
        final Node node = nodes.item(i);
        LOG.debug("node: " + node);
        if (filterName.equals(node.getTextContent())) {
          final Node filterMappingNode = node.getParentNode().getParentNode();
          LOG.debug("filterMappingNode: " + filterMappingNode);
          urlPattern = urlPatternExpression.evaluate(filterMappingNode);
          LOG.debug("urlPattern: " + urlPattern);
        }
      }

      final String prefixUppered = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);

      // Check for leading '/' and trailing '*'.
      if (!urlPattern.startsWith("/") || !urlPattern.endsWith("*")) {
        throw new IllegalArgumentException("<" + mapping + "> for WroFilter" + prefixUppered + " \"" + filterName
          + "\" must start with '/' and end with '*'.");
      }

      // Strip trailing '*' and keep leading '/'.
      return stripWildcard(urlPattern);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      throw new ServletException("Error finding <" + prefix + "> " + filterName + " in web.xml", e);
    }
  }


  /**
   * Strip trailing '*' and keep leading '/'
   *
   * @param result
   * @return The stripped String
   */
  private static String stripWildcard(final String result) {
    return result.substring(1, result.length() - 1);
  }

  /**
   * A factory method for creating a {@link ResourceProcessor} based on provided {@link ResourcePreProcessor}.
   * @param preProcessor {@link ResourcePreProcessor} to use as a {@link ResourceProcessor}.
   * @return instance of {@link ResourceProcessor}.
   */
  public static ResourceProcessor newResourceProcessor(final ResourcePreProcessor preProcessor) {
    return new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        preProcessor.process(null, reader, writer);
      }
    };
  }

  /**
   * A factory method for creating a {@link ResourceProcessor} based on provided {@link ResourcePostProcessor}.
   * @param postProcessor {@link ResourcePostProcessor} to use as a {@link ResourceProcessor}.
   * @return instance of {@link ResourceProcessor}.
   */
  public static ResourceProcessor newResourceProcessor(final ResourcePostProcessor postProcessor) {
    return new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        postProcessor.process(reader, writer);
      }
    };
  }
}
