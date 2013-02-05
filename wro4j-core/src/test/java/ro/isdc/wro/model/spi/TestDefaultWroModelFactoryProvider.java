package ro.isdc.wro.model.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestDefaultWroModelFactoryProvider {
  private DefaultModelFactoryProvider victim;
  @Before
  public void setUp() {
    victim = new DefaultModelFactoryProvider();
  }

  @Test
  public void shouldProvideOneModelFactory() {
    assertEquals(1, victim.provideModelFactories().size());
  }
}
