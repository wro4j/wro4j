package ro.isdc.wro.http.handler.factory;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;

import ro.isdc.wro.http.handler.RequestHandler;


/**
 * A {@link RequestHandlerFactory} which holds {@link RequestHandler}'s in a list.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class SimpleRequestHandlerFactory
    implements RequestHandlerFactory {
  private Collection<RequestHandler> handlers = new ArrayList<RequestHandler>();

  public final Collection<RequestHandler> create() {
    return handlers;
  }

  /**
   * Sets a not null collection of handlers.
   */
  public final SimpleRequestHandlerFactory setHandlers(final Collection<RequestHandler> handlers) {
    notNull(handlers);
    this.handlers = handlers;
    return this;
  }

  /**
   * Adds a single handler to existing collection.
   *
   * @param handler
   *          a not null {@link RequestHandler}
   */
  public final SimpleRequestHandlerFactory addHandler(final RequestHandler handler) {
    notNull(handler);
    this.handlers.add(handler);
    return this;
  }
}
