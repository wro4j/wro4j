package ro.isdc.wro.manager.runnable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * @author Alex Objelean
 */
public class TestReloadCacheRunnable {
  @Mock
  private CacheStrategy<?, ?> mockCacheStrategy;
  private ReloadCacheRunnable victim;
  
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
