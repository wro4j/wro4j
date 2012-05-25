package ro.isdc.wro.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.util.WroUtil;


/**
 * This RequestHandler will reload the model on HTTP requests to "wroAPU/reloadModel"
 * 
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class ReloadModelRequestHandler
    implements RequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadCacheRequestHandler.class);
  /**
   * wro API mapping path. If request uri contains this, exposed API method will be invoked.
   */
  public static final String PATH_API = "wroAPI";
  /**
   * API - reload model method call
   */
  public static final String API_RELOAD_MODEL = PATH_API + "/reloadModel";
  @Inject
  private Context context;
  
  /**
   * {@inheritDoc}
   */
  public void handle(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    context.getConfig().reloadModel();
    WroUtil.addNoCacheHeaders(response);
    response.setStatus(HttpServletResponse.SC_OK);
    LOG.debug("WroModel reloaded");
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean accept(HttpServletRequest request) {
    return WroUtil.matchesUrl(request, API_RELOAD_MODEL);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return context.getConfig().isDebug();
  }
}
