package ro.isdc.wro.http.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * A handler which implements all the methods. This handler accept all requests and is enabled by default. The handle
 * method doesn't do anything. This class should be used as a base class for all implementation.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class RequestHandlerSupport
    implements RequestHandler {
  /**
   * A recommended context path which expose various api calls using {@link RequestHandler} implementations.
   */
  public static final String PATH_API = "wroAPI";

  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
  }

  public boolean accept(final HttpServletRequest request) {
    return true;
  }

  /**
   * Enabled by default.
   */
  public boolean isEnabled() {
    return true;
  }
}
