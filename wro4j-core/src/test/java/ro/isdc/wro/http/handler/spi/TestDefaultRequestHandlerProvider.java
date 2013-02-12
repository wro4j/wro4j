package ro.isdc.wro.http.handler.spi;

import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestDefaultRequestHandlerProvider {
  @Mock
  private HttpServletRequest mockRequest;
  private DefaultRequestHandlerProvider victim;
  @Before
  public void setUp() {
    initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new DefaultRequestHandlerProvider();
    WroTestUtils.createInjector().inject(victim);
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
      WroTestUtils.createInjector().inject(handler);
      handler.accept(mockRequest);
      handler.isEnabled();
    }
  }
}
