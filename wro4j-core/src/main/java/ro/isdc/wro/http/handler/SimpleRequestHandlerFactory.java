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
  public final void setHandlers(final Collection<RequestHandler> handlers) {
    Validate.notNull(handlers);
    this.handlers = handlers;
  }
}
