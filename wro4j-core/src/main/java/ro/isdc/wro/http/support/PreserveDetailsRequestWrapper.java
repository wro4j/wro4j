package ro.isdc.wro.http.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * A wrapper which preserve details of the wrapped request. This is useful when the wrapper is reused outside of the
 * request cycle and the client code needs details about its original path, uri, etc.
 *
 * @author Alex Objelean
 * @created 13 Jan 2014
 * @since 1.7.3
 */
public class PreserveDetailsRequestWrapper
    extends HttpServletRequestWrapper {
  private final String requestURI;
  private final StringBuffer requestURL;
  private final String pathInfo;
  private final String contextPath;
  private final String servletPath;
  private final String pathTranslated;
  private final String queryString;
  private final String protocol;
  private final String scheme;
  private final String serverName;
  private final int serverPort;

  public PreserveDetailsRequestWrapper(final HttpServletRequest request) {
    super(request);
    requestURI = request.getRequestURI();
    requestURL = request.getRequestURL();
    pathInfo = request.getPathInfo();
    contextPath = request.getContextPath();
    servletPath = request.getServletPath();
    pathTranslated = request.getPathTranslated();
    queryString = request.getQueryString();
    protocol = request.getProtocol();
    scheme = request.getScheme();
    serverName = request.getServerName();
    serverPort = request.getServerPort();
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public String getRequestURI() {
    return requestURI;
  }

  @Override
  public StringBuffer getRequestURL() {
    return requestURL;
  }

  @Override
  public String getPathInfo() {
    return pathInfo;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public String getServletPath() {
    return servletPath;
  }

  @Override
  public String getPathTranslated() {
    return pathTranslated;
  }

  @Override
  public String getQueryString() {
    return queryString;
  }
}
