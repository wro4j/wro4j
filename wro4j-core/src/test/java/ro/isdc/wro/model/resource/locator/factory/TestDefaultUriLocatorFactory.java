package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator.LocatorStrategy;


/**
 * @author Alex Objelean
 */
public class TestDefaultUriLocatorFactory {
  private DefaultResourceLocatorFactory victim;

  @Before
  public void setUp() {
    victim = new DefaultResourceLocatorFactory();
  }

  @Test
  public void shouldHaveSeveralDefaultLocators() {
    assertEquals(5, victim.getLocatorFactories().size());
  }

  @Test
  public void shouldUseServletContextLocatorWithPreferredLocatorStrategy() {
    for (final ResourceLocatorFactory locator : victim.getLocatorFactories()) {
      if (locator instanceof ServletContextResourceLocatorFactory) {
        final LocatorStrategy actual = ((ServletContextResourceLocator) locator.getLocator("/someUri")).getLocatorStrategy();
        assertEquals(LocatorStrategy.DISPATCHER_FIRST, actual);
        return;
      }
    }
  }
}
