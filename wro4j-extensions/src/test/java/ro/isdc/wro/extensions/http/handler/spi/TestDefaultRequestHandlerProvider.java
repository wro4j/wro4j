package ro.isdc.wro.extensions.http.handler.spi;

import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestDefaultRequestHandlerProvider {
  @Mock
  private HttpServletRequest mockRequest;
  private DefaultRequestHandlerProvider victim;
  private Injector injector;
  @Before
  public void setUp() {
    initMocks(this);
    Context.set(Context.standaloneContext());
    injector = WroTestUtils.createInjector();
    victim = new DefaultRequestHandlerProvider();
    injector.inject(victim);
  }

  @Test
  public void shoudProvideAtLeastOneRequestHandler() {
    final Map<String, RequestHandler> map = victim.provideRequestHandlers();
    assertFalse(map.isEmpty());
  }

  @Test
  public void shouldNotFailWhenEachHandlerIsInvoked() {
    final Map<String, RequestHandler> map = victim.provideRequestHandlers();
    for(final RequestHandler handler : map.values()) {
      injector.inject(handler);
      handler.accept(mockRequest);
      handler.isEnabled();
    }
  }
}
