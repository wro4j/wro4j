package ro.isdc.wro.http.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;


/**
 * <p>A wrapper which preserve details of the wrapped request. In other words, it doesn't delegate the call to getters, but
 * rather caches the values locally during construction and returns cached values. This is useful when the wrapper is
 * reused outside of the request cycle and the client code needs details about its original path, uri, etc. As result,
 * even if the request cycle completes and the wrapped request is returned to the container request pool, the wrapper
 * can refer these details without any issue.</p>
 *
 * <p>One important thing to notice, is that the wrapper is not safe to use if you want to access dispatcher, inputStream,
 * parameters, attributes or cookies.</p>
 *
 * @author Alex Objelean
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
    //get the immutable representation of buffer, because StringBuffer is mutable.
    requestURL = new StringBuffer(request.getRequestURL().toString());
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
