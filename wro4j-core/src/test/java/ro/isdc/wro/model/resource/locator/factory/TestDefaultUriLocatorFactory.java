package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator.LocatorStrategy;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * @author Alex Objelean
 */
public class TestDefaultUriLocatorFactory {
  private DefaultUriLocatorFactory victim;

  @Before
  public void setUp() {
    victim = new DefaultUriLocatorFactory();
  }

  @Test
  public void shouldHaveSeveralDefaultLocators() {
    assertEquals(5, victim.getUriLocators().size());
  }

  @Test
  public void shouldUseServletContextLocatorWithPreferredLocatorStrategy() {
    for (final UriLocator locator : victim.getUriLocators()) {
      if (locator instanceof ServletContextUriLocator) {
        assertEquals(LocatorStrategy.DISPATCHER_FIRST, ((ServletContextUriLocator) locator).getLocatorStrategy());
        return;
      }
    }
  }
}
