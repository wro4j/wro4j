package ro.isdc.wro.model.resource.support.hash;

import org.junit.Before;

/**
 * @author Alex Objelean
 */
public class ConfigurableHashBuilderTest {
  private ConfigurableHashStrategy victim;
  
  @Before
  public void setUp() {
    victim = new ConfigurableHashStrategy();
  }
}
