package ro.isdc.wro.extensions.http.handler;

import java.io.IOException;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.RequestHandlerSupport;
import ro.isdc.wro.http.support.ResponseHeadersConfigurer;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.WroUtil;


/**
 * Expose the {@link WroModel} as JSON when the following uri is accessed: "wroAPI/model". This {@link RequestHandler}
 * is useful to inspect the model and to simulate the behavior of the page when all resources are included (one by one)
 * without being merged.
 *
 * @author Alex Objelean
 * @author Ivar Conradi Østhus
 * @since 1.4.7
 */
public class ModelAsJsonRequestHandler
    extends RequestHandlerSupport {
  static final String CONTENT_TYPE = "application/json";
  /**
   * API - reload cache method call
   */
  public static final String ENDPOINT_URI = PATH_API + "/model";
  /**
   * The alias of this {@link RequestHandler} used for configuration.
   */
  public static final String ALIAS = "modelAsJson";
  @Inject
  private ReadOnlyContext context;

  @Inject
  private WroModelFactory modelFactory;

  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    // Set header
    ResponseHeadersConfigurer.noCache().setHeaders(response);
    response.setContentType(CONTENT_TYPE);
    response.setStatus(HttpServletResponse.SC_OK);

    // Build content
    newGson(request).toJson(modelFactory.create(), response.getWriter());
    response.getWriter().flush();
  }

  /**
   * @return customized {@link Gson} instance.
   */
  protected Gson newGson(final HttpServletRequest request) {
    return new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceSerializer(getWroBasePath(request))).setPrettyPrinting().disableHtmlEscaping().create();
  }

  @Override
  public boolean accept(final HttpServletRequest request) {
    return WroUtil.matchesUrl(request, ENDPOINT_URI);
  }

  private String getWroBasePath(final HttpServletRequest request) {
    return request.getRequestURI().replaceAll("(?i)" + ENDPOINT_URI, "");
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && context.getConfig().isDebug();
  }
}
