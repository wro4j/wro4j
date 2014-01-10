package ro.isdc.wro.http.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Ivar Conradi Østhus
 */
public class TestResourceWatcherRequestHandler {
  private ResourceWatcherRequestHandler victim;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ResourceWatcherRequestHandler();

    Context.set(Context.webContext(request, response, mock(FilterConfig.class)));
    WroTestUtils.createInjector().inject(victim);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldHandleRequest() {
    when(request.getAttribute(DispatcherStreamLocator.ATTRIBUTE_INCLUDED_BY_DISPATCHER)).thenReturn(Boolean.TRUE);
    when(request.getRequestURI()).thenReturn("wroAPI/resourceWatch");
    assertTrue(victim.accept(request));
  }

  @Test
  public void shouldNotHandleRequestThatIsNotDispatchedByServer() {
    when(request.getRequestURI()).thenReturn("wroAPI/resourceWatch");
    assertFalse(victim.accept(request));
  }

  @Test
  public void shouldNotHandleRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/somethingElse");
    assertFalse(victim.accept(request));
  }
}
