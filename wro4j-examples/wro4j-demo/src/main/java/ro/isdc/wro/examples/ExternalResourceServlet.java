/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.model.WroModel;

/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ExternalResourceServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalResourceServlet.class);

  
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {
    ServletContextAttributeHelper helper = new ServletContextAttributeHelper(getServletContext());

    WroModel model = helper.getManagerFactory().create().getModelFactory().create();
    LOG.debug("model: " + model);
    resp.sendRedirect("http://code.jquery.com/jquery-1.4.4.js");
  }
}
