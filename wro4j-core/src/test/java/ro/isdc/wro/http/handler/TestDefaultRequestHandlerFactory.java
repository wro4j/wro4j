package ro.isdc.wro.http.handler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;


public class TestDefaultRequestHandlerFactory {
  private DefaultRequestHandlerFactory defaultRequestHandlerFactory;
  
  @Before
  public void setup() {
    defaultRequestHandlerFactory = new DefaultRequestHandlerFactory();
  }
  
  @Test
  public void shouldCreateListOfDefaultRequestHandlers() {
    Collection<RequestHandler> requestHandlers = defaultRequestHandlerFactory.create();
    assertThat(requestHandlers.isEmpty(), is(false));
  }
}
