package ro.isdc.wro.manager.runnable;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.factory.WroModelFactory;


/**
 * @author Alex Objelean
 */
public class TestReloadModelRunnable {
  @Mock
  private WroModelFactory mockModelFactory;
  private ReloadModelRunnable victim;
  
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
