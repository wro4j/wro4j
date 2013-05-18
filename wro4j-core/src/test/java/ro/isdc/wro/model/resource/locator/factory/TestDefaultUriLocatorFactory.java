package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
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
    assertEquals(5, victim.getUriLocators().size());
  }

  @Test
  public void shouldUseServletContextLocatorWithPreferredLocatorStrategy() {
    for (final ResourceLocator locator : victim.getUriLocators()) {
      if (locator instanceof ServletContextResourceLocator) {
        assertEquals(LocatorStrategy.DISPATCHER_FIRST, ((ServletContextResourceLocator) locator).getLocatorStrategy());
        return;
      }
    }
  }
}
