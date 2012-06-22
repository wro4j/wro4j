package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A handler which implements all the methods. This handler accept all requests and is enabled by default. The handle
 * method doesn't do anything. This class should be used as a base class for all implementation.
 * 
 * @author Alex Objelean
 * @created 31 May 2012
 * @since 1.4.7
 */
public class RequestHandlerSupport
    implements RequestHandler {
  /**
   * A recommended context path which expose various api calls using {@link RequestHandler} implementations.
   */
  public static final String PATH_API = "wroAPI";
  
  /**
   * {@inheritDoc}
   */
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean accept(final HttpServletRequest request) {
    return true;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return true;
  }
  
}
