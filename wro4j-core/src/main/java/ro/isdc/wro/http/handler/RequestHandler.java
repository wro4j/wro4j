package ro.isdc.wro.http.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Handler interface for components that process HTTP request. Typically implemented to provide api functionality for
 * the WroFilter. Handlers are applied before the processing in the WroFilter, and only one handler can be applied. If
 * one handler is applied, other handlers are skipped and processing is not performed.
 *
 * It is recommended to accept requests for the uri's containing the following path:
 * {@link RequestHandlerSupport#PATH_API}.
 * 
 * @author Ivar Conradi Østhus
 * @since 1.4.7
 */
public interface RequestHandler {
  
  /**
   * Handle the given request, generating a response.
   * 
   * @param request
   *          current HTTP request
   * @param response
   *          current HTTP response
   * @throws IOException
   *           in case of I/O errors
   */
  void handle(HttpServletRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * Determines if current request can be handled by this requestHandler
   * @param request current HTTP request
   * @return true if this requestHandler should handle this request
   */
  boolean accept(HttpServletRequest request);

  /**
   * Used to determine if the RequestHandler is enabled
   */
  boolean isEnabled();
}
