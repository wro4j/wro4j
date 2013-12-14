package ro.isdc.wro.manager.runnable;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestReloadCacheRunnable {
  @Mock
  private CacheStrategy<?, ?> mockCacheStrategy;
  private ReloadCacheRunnable victim;
  
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
    victim = new ReloadCacheRunnable(mockCacheStrategy);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument() {
    new ReloadCacheRunnable(null);
  }
  
  @Test
  public void shouldDestroyModelWhenInvokingRun() {
    victim.run();
    Mockito.verify(mockCacheStrategy).clear();
  }
}
