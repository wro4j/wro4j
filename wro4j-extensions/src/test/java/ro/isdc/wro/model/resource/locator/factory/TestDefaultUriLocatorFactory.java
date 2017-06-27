package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;
import ro.isdc.wro.extensions.locator.WebjarsUriLocator;
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
    assertEquals(9, victim.getUriLocators().size());
  }

  @Test
  public void shouldContainWebjarLocator() {
    assertTrue(contains(victim.getUriLocators(), Predicates.instanceOf(WebjarUriLocator.class)));
    assertTrue(contains(victim.getUriLocators(), Predicates.instanceOf(WebjarsUriLocator.class)));
  }

  private boolean contains(final List<UriLocator> collection, final Predicate<Object> predicate) {
    boolean hasAny = false;
    for (final UriLocator locator : victim.getUriLocators()) {
      if (predicate.apply(locator)) {
        hasAny = true;
        break;
      }
    }
    return hasAny;
  }
}
