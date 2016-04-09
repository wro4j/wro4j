package ro.isdc.wro.manager.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;


public class TestWroManagerFactoryDecorator {
  @Mock
  private WroManagerFactory decorated;
  private WroManagerFactoryDecorator victim;

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
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    when(decorated.create()).thenReturn(new WroManager.Builder().build());
    victim = new WroManagerFactoryDecorator(decorated);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldInvokeCreateOnDecoratedFactory() {
    victim.create();
    verify(decorated).create();
  }

  @Test
  public void shouldInvokeDestroyOnDecoratedFactory() {
    victim.destroy();
    verify(decorated).destroy();
  }

  @Test
  public void shouldInitializeStandaloneContextWhenDecoratedIsStandaloneContextAware() {
    final DefaultStandaloneContextAwareManagerFactory decorated = Mockito.spy(new DefaultStandaloneContextAwareManagerFactory());
    victim = new WroManagerFactoryDecorator(decorated);

    final StandaloneContext standaloneContext = new StandaloneContext();
    victim.initialize(standaloneContext);

    Mockito.verify(decorated).initialize(Mockito.eq(standaloneContext));
  }
}
