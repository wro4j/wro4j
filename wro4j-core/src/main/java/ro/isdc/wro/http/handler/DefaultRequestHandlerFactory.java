package ro.isdc.wro.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultRequestHandlerFactory implements RequestHandlerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestHandlerFactory.class);

  public Collection<RequestHandler> create() {
    Collection<RequestHandler> requestHandlers = new ArrayList<RequestHandler>();
    requestHandlers.add(new ReloadCacheRequestHandler());
    requestHandlers.add(new ReloadModelRequestHandler());
    LOG.debug("default request handlers created");

    return requestHandlers;
  }
}
