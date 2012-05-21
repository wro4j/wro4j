package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.util.WroUtil;


/**
 * This RequestHandler will reload the model on HTTP requests to "wroAPU/reloadModel"
 * 
 * @author Ivar Conradi Østhus
 * @created Created on May 19, 2012
 */
public class ReloadModelRequestHandler
    implements RequestHandler {
  /**
   * wro API mapping path. If request uri contains this, exposed API method will be invoked.
   */
  public static final String PATH_API = "wroAPI";
  /**
   * API - reload model method call
   */
  public static final String API_RELOAD_MODEL = PATH_API + "/reloadModel";
  
  /**
   * {@inheritDoc}
   */
  public void handle(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    getWroConfiguration().reloadModel();
    WroUtil.addNoCacheHeaders(response);
    response.setStatus(HttpServletResponse.SC_OK);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean accept(HttpServletRequest request) {
    return getWroConfiguration().isDebug() && WroUtil.matchesUrl(request, API_RELOAD_MODEL);
  }
  
  /**
   * {@inheritDoc}
   */
  public void enable() {
    throw new RuntimeException("Not implemented jet!");
  }
  
  private WroConfiguration getWroConfiguration() {
    return Context.get().getConfig();
  }
}
