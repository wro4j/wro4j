/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.DispatcherStreamLocator;

/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class DispatchResourceServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(DispatchResourceServlet.class);
  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {
    final DispatcherStreamLocator streamLocator = new DispatcherStreamLocator();

    final StringBuffer combo = new StringBuffer();

    String location = "/wro/dwr.js";


    InputStream stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
    combo.append(IOUtils.toString(stream));

    location = "/wro/jsp.css";
    stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
    combo.append(IOUtils.toString(stream));

    location = "/wro/wicket.js";
    stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
    combo.append(IOUtils.toString(stream));

    resp.getWriter().write(combo.toString());
    resp.setContentType(ResourceType.JS.getContentType());
    resp.getWriter().close();
  }

  private HttpServletRequest wrapRequestForNoGzip(final HttpServletRequest req) {
    return new HttpServletRequestWrapper(req) {
      @Override
      public Enumeration getHeaderNames() {
        return null;
      }
    };
  }
}
