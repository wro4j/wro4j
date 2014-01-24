package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;


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
  private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerSupport.class);
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
   * Enabled by default if the {@link Context} is set.
   */
  public boolean isEnabled() {
    if (!Context.isContextSet()) {
      LOG.debug("Context NOT set. Thread ID: {}", Thread.currentThread().getName());
    }
    return Context.isContextSet();
  }

}
