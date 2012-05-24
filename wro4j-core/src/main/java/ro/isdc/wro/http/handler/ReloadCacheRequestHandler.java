package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.jmx.WroConfigurationMBean;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.util.WroUtil;


/**
 * This RequestHandler will reload the cache on HTTP requests to "wroAPU/reloadCache"
 * 
 * @author Ivar Conradi Østhus
 * @created Created on May 19, 2012
 */
public class ReloadCacheRequestHandler
    implements RequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadCacheRequestHandler.class);
  
  /**
   * wro API mapping path. If request uri contains this, exposed API method will be invoked.
   */
  public static final String PATH_API = "wroAPI";
  /**
   * API - reload cache method call
   */
  public static final String API_RELOAD_CACHE = PATH_API + "/reloadCache";

  /**
   * {@inheritDoc}
   */
  public void handle(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    getConfig().reloadCache();
    WroUtil.addNoCacheHeaders(response);
    response.setStatus(HttpServletResponse.SC_OK);
    LOG.debug("Cache is reloaded");
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean accept(HttpServletRequest request) {
    return WroUtil.matchesUrl(request, API_RELOAD_CACHE);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return getConfig().isDebug();
  }

  private WroConfigurationMBean getConfig() {
    return Context.get().getConfig();
  }
}
