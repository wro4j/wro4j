package ro.isdc.wro.http.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * A {@link HttpServletRequestWrapper} implementation, which store few request info inside its instance fields, allowing
 * {@link InheritableThreadLocal} holding reference to {@link HttpServletRequest} to access these information even
 * outside request cycle.
 * <p/>
 * This is useful when a thread is launched not during the servlet processing, and we still need to know what are the
 * requestURI, requestURL & servletPath of the request which was the parent of this thread. If this wrapper is not used,
 * the request fields will be nullified by the container.
 * 
 * @author Alex Objelean
 */
public class FieldsSavingRequestWrapper
    extends HttpServletRequestWrapper {
  private final String requestURI;
  private final StringBuffer requestURL;
  private final String servletPath;
  
  /**
   * @param request
   */
  public FieldsSavingRequestWrapper(final HttpServletRequest request) {
    super(request);
    this.requestURI = request.getRequestURI();
    this.requestURL = request.getRequestURL();
    this.servletPath = request.getServletPath();
  }
  
  /**
   * @return the requestURI
   */
  @Override
  public String getRequestURI() {
    return this.requestURI;
  }
  
  /**
   * @return the requestURL
   */
  @Override
  public StringBuffer getRequestURL() {
    return this.requestURL;
  }
  
  /**
   * @return the servletPath
   */
  @Override
  public String getServletPath() {
    return this.servletPath;
  }
}