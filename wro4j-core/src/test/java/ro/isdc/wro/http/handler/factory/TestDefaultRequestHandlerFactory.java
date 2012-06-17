package ro.isdc.wro.http.handler.factory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.factory.DefaultRequestHandlerFactory;


/**
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 */
public class TestDefaultRequestHandlerFactory {
  private DefaultRequestHandlerFactory victim;
  
  @Before
  public void setup() {
    victim = new DefaultRequestHandlerFactory();
  }
  
  @Test
  public void shouldCreateListOfDefaultRequestHandlers() {
    Collection<RequestHandler> requestHandlers = victim.create();
    assertThat(requestHandlers.isEmpty(), is(false));
  }
}
