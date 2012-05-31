package ro.isdc.wro.http.handler;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.Validate;


/**
 * A {@link RequestHandlerFactory} which holds {@link RequestHandler}'s in a list.
 * 
 * @author Alex Objelean
 * @created 25 May 2012
 * @since 1.4.7
 */
public class SimpleRequestHandlerFactory
    implements RequestHandlerFactory {
  private Collection<RequestHandler> handlers = new ArrayList<RequestHandler>();
  
  /**
   * {@inheritDoc}
   */
  public final Collection<RequestHandler> create() {
    return handlers;
  }
  
  /**
   * Sets a not null collection of handlers.
   */
  public final SimpleRequestHandlerFactory setHandlers(final Collection<RequestHandler> handlers) {
    Validate.notNull(handlers);
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
    Validate.notNull(handler);
    this.handlers.add(handler);
    return this;
  }
}
