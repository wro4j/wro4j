package ro.isdc.wro.http.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
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
  @Mock
  private ResourceWatcher resourceWatcher;

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
  public void shouldAcceptAndHandleAuthorizedRequest() throws Exception {
    final String authKey = "123";
    victim = new ResourceWatcherRequestHandler() {
      @Override
      String generateRandomKey() {
        return authKey;
      }
    };
    final CacheKey expected = new CacheKey("group", ResourceType.CSS);
    new InjectorBuilder(new BaseWroManagerFactory()).setResourceWatcher(resourceWatcher).build().inject(victim);
    when(request.getParameter(Mockito.eq(ResourceWatcherRequestHandler.PATH_API))).thenReturn(
        ResourceWatcherRequestHandler.PATH_HANDLER);
    when(request.getParameter(Mockito.eq(ResourceWatcherRequestHandler.PARAM_GROUP_NAME))).thenReturn(
        expected.getGroupName());
    when(request.getParameter(Mockito.eq(ResourceWatcherRequestHandler.PARAM_RESOURCE_TYPE))).thenReturn(
        expected.getType().name());
    when(request.getParameter(Mockito.eq(ResourceWatcherRequestHandler.PARAM_AUTH_KEY))).thenReturn(authKey);
    when(request.getRequestURI()).thenReturn("/style.css?wroAPI=resourceWatcher");
    assertTrue(victim.accept(request));

    victim.handle(request, response);

    verify(resourceWatcher).check(Mockito.eq(expected));
  }

  @Test
  public void shouldNotAcceptRequestThatIsNotDispatchedByServer() {
    when(request.getRequestURI()).thenReturn("wroAPI/resourceWatch");
    assertFalse(victim.accept(request));
  }

  @Test
  public void shouldNotAcceptRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/somethingElse");
    assertFalse(victim.accept(request));
  }
}
