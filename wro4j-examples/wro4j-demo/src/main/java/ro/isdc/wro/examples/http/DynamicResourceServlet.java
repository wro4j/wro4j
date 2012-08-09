/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.http;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.model.resource.ResourceType;


/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class DynamicResourceServlet
    extends HttpServlet {
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType(ResourceType.JS.getContentType());
    final String result = "document.write('<h1>" + dateFormat.format(new Date()) + "</h1>');";
    resp.getWriter().write(result);
  }
}
