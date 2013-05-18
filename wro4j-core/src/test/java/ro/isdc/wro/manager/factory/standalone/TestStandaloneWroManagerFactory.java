package ro.isdc.wro.manager.factory.standalone;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.SimpleResourceLocatorFactory;

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
    final ResourceLocatorFactory locatorFactory = victim.newLocatorFactory();
    final List<?> availableLocators = ((SimpleResourceLocatorFactory) locatorFactory).getUriLocators();
    final List<?> defaultLocators = new DefaultResourceLocatorFactory().getUriLocators();
    Assert.assertTrue(availableLocators.size() > defaultLocators.size());
  }
}
