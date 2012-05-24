package ro.isdc.wro.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultRequestHandlerFactory implements RequestHandlerFactory {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public Collection<RequestHandler> create() {
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    logger.debug("default request handlers created");

    return requestHandlers;
  }
}
