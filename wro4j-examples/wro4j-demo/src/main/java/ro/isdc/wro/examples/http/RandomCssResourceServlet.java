/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.http;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.model.resource.ResourceType;


/**
 * Produces a css content with two classes (called .random-color and .random-background) which defines a random color
 * and background useful for testing dynamic behavior.
 * 
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class RandomCssResourceServlet
    extends HttpServlet {
  private final Random random = new Random();
  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType(ResourceType.CSS.getContentType());
    final String result = String.format(".random-color { color: %s;} .random-background { background: %s;}",
        getRandomColor(), getRandomColor());
    resp.getWriter().write(result);
  }
  
  private String getRandomColor() {
    return String.format("rgb(%s,%s,%s)", random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }
}
