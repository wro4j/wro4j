package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;
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
    assertEquals(6, victim.getUriLocators().size());
  }

  @Test
  public void shouldContainWebjarLocator() {
    boolean hasWebjarLocator = false;
    for (final UriLocator locator : victim.getUriLocators()) {
      if (locator instanceof WebjarUriLocator) {
        hasWebjarLocator = true;
      }
    }
    assertTrue(hasWebjarLocator);
  }
}
