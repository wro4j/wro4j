package ro.isdc.wro.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.config.factory.ServletContextPropertyWroConfigurationFactory;

import javax.servlet.ServletContext;
import java.util.*;

public class RequestHandlerFactory {
  public static final String REQUEST_HANDLERS = "requestHandlers";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ServletContext servletContext;

  public RequestHandlerFactory(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public Collection<RequestHandler> create() {
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    String requestHandlersName = getRequestHandlerNames();
    if(requestHandlersName == null) {
      addDefaultRequestHandlers(requestHandlers);
    } else {
      //Todo: Not implemented jet
    }
    return requestHandlers;
  }

  private void addDefaultRequestHandlers(Collection<RequestHandler> requestHandlers) {
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    logger.debug("default request handlers created");
  }

  private String getRequestHandlerNames() {
    return (String)getProperties().get(REQUEST_HANDLERS);
  }

  private Properties getProperties() {
      // default location is /WEB-INF/wro.properties
      final Properties props = new Properties();
      try {
        return new ServletContextPropertyWroConfigurationFactory(servletContext).createProperties();
      } catch (final Exception e) {
        logger.debug("No configuration property file found.");
      }
      return props;
    }

}
