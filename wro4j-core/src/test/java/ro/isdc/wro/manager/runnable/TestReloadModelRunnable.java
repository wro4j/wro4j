package ro.isdc.wro.manager.runnable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.model.factory.WroModelFactory;


/**
 * @author Alex Objelean
 */
public class TestReloadModelRunnable {
  @Mock
  private WroModelFactory mockModelFactory;
  private ReloadModelRunnable victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ReloadModelRunnable(mockModelFactory);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument() {
    new ReloadModelRunnable(null);
  }
  
  @Test
  public void shouldDestroyModelWhenInvokingRun() {
    victim.run();
    Mockito.verify(mockModelFactory).destroy();
  }
}
