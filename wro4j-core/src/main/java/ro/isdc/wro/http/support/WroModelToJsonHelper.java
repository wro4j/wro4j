package ro.isdc.wro.http.support;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * WroModelToJsonHelper is uesd by WroFilter to expose the wroModel as a json API.
 *
 */
public class WroModelToJsonHelper extends HttpServlet {

  public static void produceJson(final HttpServletResponse response, final WroManagerFactory wroManagerFactory)
          throws ServletException, IOException {
    WroModel model = wroManagerFactory.create().getModelFactory().create();

    //Set header
    response.setContentType("application/json");

    //Build content
    Gson gson = new Gson();
    gson.toJson(model, response.getWriter());
    response.getWriter().flush();
  }
}
