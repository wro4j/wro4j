package ro.isdc.wro.manager.runnable;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcherRunnable {
  private ResourceWatcherRunnable victim;
  
  @Before
  public void setUp() {
    victim = new ResourceWatcherRunnable(WroTestUtils.createInjector());
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument() {
    new ResourceWatcherRunnable(null);
  }
  
  @Test
  public void test() {
  }
}
