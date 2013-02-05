package ro.isdc.wro.model.factory;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;

/**
 * @author Alex Objelean
 */
public class TestConfigurableModelFactory {
  private ConfigurableModelFactory victim;
  @Before
  public void setUp() {
    victim = new ConfigurableModelFactory();
  }

  @Test
  public void shouldHaveOneAvailableStrategy() {
    assertEquals(1, victim.getAvailableStrategies().size());

    final Set<String> expectedAliases = new HashSet<String>();
    expectedAliases.add(XmlModelFactory.ALIAS);
    assertEquals(expectedAliases, victim.getAvailableAliases());
  }

  @Test
  public void shouldUseCorrectDefaultStrategy() {
    assertEquals(XmlModelFactory.class, victim.getDefaultStrategy().getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidStrategy() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableModelFactory.KEY, "Invalid");
    victim.setProperties(props);
    victim.getConfiguredStrategy();
  }
}
