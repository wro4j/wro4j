/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A processor responsible for handling a request.
 * @author Alex Objelean
 */
public interface RequestProcessor {
  /**
   * @param request {@link HttpServletRequest} to process.
   * @return true if the {@link RequestProcessor} knows how to handle the request.
   */
  boolean accept(final HttpServletRequest request);

  /**
   * Perform actual processing logic.
   * @param request {@link HttpServletRequest} to process.
   * @param response {@link HttpServletResponse} response object corresponding to current request cycle.
   * @return {@link InputStream} that streams the processing result
   */
  InputStream process(final HttpServletRequest request, final HttpServletResponse response);
}
