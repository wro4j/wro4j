package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.locator.WebjarResourceLocator;


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
    assertEquals(6, victim.getLocatorFactories().size());
  }

  @Test
  public void shouldContainWebjarLocator() {
    boolean hasWebjarLocator = false;
    for (final ResourceLocatorFactory locator : victim.getLocatorFactories()) {
      if (locator instanceof WebjarResourceLocator) {
        hasWebjarLocator = true;
      }
    }
    assertTrue(hasWebjarLocator);
  }
}
