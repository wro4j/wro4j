package ro.isdc.wro.http.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.support.ResponseHeadersConfigurer;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.util.WroUtil;


/**
 * This RequestHandler will reload the model on HTTP requests to "wroAPI/reloadModel"
 *
 * This handler is available only in debug mode by default. You can change this behavior by overriding
 * {@link RequestHandler#isEnabled()} method.
 *
 * @author Ivar Conradi Østhus
 * @since 1.4.7
 */
public class ReloadModelRequestHandler
    extends RequestHandlerSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ReloadModelRequestHandler.class);
  /**
   * API - reload model method call
   */
  public static final String ENDPOINT_URI = PATH_API + "/reloadModel";
  /**
   * The alias of this {@link RequestHandler} used for configuration.
   */
  public static final String ALIAS = "reloadModel";
  @Inject
  private ReadOnlyContext context;

  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    Context.get().getConfig().reloadModel();
    ResponseHeadersConfigurer.noCache().setHeaders(response);
    response.setStatus(HttpServletResponse.SC_OK);
    LOG.debug("WroModel reloaded");
  }

  @Override
  public boolean accept(final HttpServletRequest request) {
    return WroUtil.matchesUrl(request, ENDPOINT_URI);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && context.getConfig().isDebug();
  }
}
