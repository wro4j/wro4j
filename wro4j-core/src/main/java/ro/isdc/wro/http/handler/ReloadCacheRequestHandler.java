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
 * This RequestHandler will reload the cache on HTTP requests to "wroAPI/reloadCache".
 * <p/>
 * This handler is available only in debug mode by default. You can change this behavior by overriding
 * {@link RequestHandler#isEnabled()} method.
 * 
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class ReloadCacheRequestHandler
    extends RequestHandlerSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadCacheRequestHandler.class);
  /**
   * API - reload cache method call
   */
  public static final String ENDPOINT_URI = PATH_API + "/reloadCache";
  @Inject
  private Context context;
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    context.getConfig().reloadCache();
    WroUtil.addNoCacheHeaders(response);
    response.setStatus(HttpServletResponse.SC_OK);
    LOG.debug("Cache is reloaded");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final HttpServletRequest request) {
    return WroUtil.matchesUrl(request, ENDPOINT_URI);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEnabled() {
    return context.getConfig().isDebug();
  }
}
