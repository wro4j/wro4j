package ro.isdc.wro.http.handler.spi;

import java.util.Map;

import ro.isdc.wro.http.handler.RequestHandler;

/**
 * A service provider responsible for providing new implementations of {@link RequestHandler}.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
public interface RequestHandlerProvider {
  /**
   * @return the {@link RequestHandler} implementations to contribute. The key represents the alias.
   */
  Map<String, RequestHandler> provideRequestHandlers();  
}
