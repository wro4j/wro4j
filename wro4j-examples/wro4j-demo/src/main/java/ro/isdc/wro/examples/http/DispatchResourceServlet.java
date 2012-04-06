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

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;

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
    final DispatcherStreamLocator streamLocator = new DispatcherStreamLocator();

    //contains a merge response of combo groups content.
    final StringBuffer combo = new StringBuffer();

    //In this case we hardcode 3 resources served by wro, in a production we would inspect the requestUri and would extract combo groups

    String location = "/resource/dynamic.js";


    InputStream stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
    combo.append(IOUtils.toString(stream));

//    location = "/wro/wildcard.js";
//    stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
//    combo.append(IOUtils.toString(stream));
//
//    location = "/wro/wicket.js";
//    stream = streamLocator.getInputStream(wrapRequestForNoGzip(req), resp, location);
//    combo.append(IOUtils.toString(stream));

    resp.getWriter().write(combo.toString());
    resp.setContentType(ResourceType.JS.getContentType());
    resp.getWriter().close();
  }

  /**
   * This decorator is required to get an unzipped response..
   */
  private HttpServletRequest wrapRequestForNoGzip(final HttpServletRequest req) {
    return new HttpServletRequestWrapper(req) {
      @Override
      public Enumeration getHeaderNames() {
        return null;
      }
    };
  }
}
