package ro.isdc.wro.extensions.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.RequestHandlerSupport;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.WroUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Expose the {@link WroModel} as JSON when the following uri is accessed: "wroAPI/model". This {@link RequestHandler}
 * is useful to inspect the model and to simulate the behavior of the page when all resources are included (one by one)
 * without being merged.
 * 
 * @author Alex Objelean
 * @author Ivar Conradi Østhus
 * @created 31 May 2012
 * @since 1.4.7
 */
public class ExposeModelRequestHandler
    extends RequestHandlerSupport {
  @VisibleForTesting
  static final String CONTENT_TYPE = "application/json";
  /**
   * wro API mapping path. If request uri contains this, exposed API method will be invoked.
   */
  public static final String PATH_API = "wroAPI";
  /**
   * API - reload cache method call
   */
  public static final String ENDPOINT_URI = PATH_API + "/model";
  @Inject
  private WroModelFactory modelFactory;
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    // Set header
    WroUtil.addNoCacheHeaders(response);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final HttpServletRequest request) {
    return WroUtil.matchesUrl(request, ENDPOINT_URI);
  }
  
  private String getWroBasePath(HttpServletRequest request) {
     return request.getRequestURI().replaceAll("(?i)" + ENDPOINT_URI, "");
   }
}
