/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.http;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class DispatchResourceServlet extends HttpServlet {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {
    final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/wro/all.js");
    dispatcher.forward(req, resp);
  }
}
