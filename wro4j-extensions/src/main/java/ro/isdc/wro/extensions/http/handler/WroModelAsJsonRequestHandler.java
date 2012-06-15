package ro.isdc.wro.extensions.http.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.WroUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * This RequestHandler will expose the current wroModel as JSON on HTTP requests to "wroAPU/model"
 * 
 * @author Ivar Conradi Østhus
 * @created 13 Jun 2012
 * @since 1.4.7
 */
public class WroModelAsJsonRequestHandler
    implements RequestHandler {
  
  public static final String WRO_API_MODEL_PATH = "wroAPI/model";
  
  @Inject
  private Context context;
  
  @Inject
  WroModelFactory wroModelFactory;
  
  /**
   * {@inheritDoc}
   */
  public void handle(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Resource.class, new ResourceSerializer(getWroBasePath(request)))
            .setPrettyPrinting()
             .disableHtmlEscaping()
            .create();
    
    WroModel model = wroModelFactory.create();
    
    // Set header
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_OK);
    
    // Build content
    gson.toJson(model, response.getWriter());
    response.getWriter().flush();
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean accept(HttpServletRequest request) {
    return WroUtil.matchesUrl(request, WRO_API_MODEL_PATH);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return context.getConfig().isDebug();
  }

  private String getWroBasePath(HttpServletRequest request) {
     return request.getRequestURI().replaceAll("(?i)" + WRO_API_MODEL_PATH, "");
   }
}
