package ro.isdc.wro.manager.factory.standalone;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

/**
 * @author Alex Objelean
 */
public class TestStandaloneWroManagerFactory {
  private StandaloneWroManagerFactory victim;

  @Before
  public void setUp() {
    victim = new StandaloneWroManagerFactory();
  }

  @Test
  public void shouldHaveMoreLocatorsThanDefaultFactoryHas() {
    final UriLocatorFactory locatorFactory = victim.newUriLocatorFactory();
    final List<?> availableLocators = ((SimpleUriLocatorFactory) locatorFactory).getUriLocators();
    final List<?> defaultLocators = new DefaultUriLocatorFactory().getUriLocators();
    Assert.assertTrue(availableLocators.size() > defaultLocators.size());
  }
}
