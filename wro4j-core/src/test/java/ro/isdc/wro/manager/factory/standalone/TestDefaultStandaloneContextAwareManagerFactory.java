package ro.isdc.wro.manager.factory.standalone;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.SimpleResourceLocatorFactory;

/**
 * @author Alex Objelean
 */
public class TestDefaultStandaloneContextAwareManagerFactory {
  private DefaultStandaloneContextAwareManagerFactory victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new DefaultStandaloneContextAwareManagerFactory();
    victim.initialize(new StandaloneContext());
  }

  @Test
  public void shouldHaveMoreLocatorsThanDefaultFactoryHas() {
    final ResourceLocatorFactory locatorFactory = victim.newLocatorFactory();
    final List<?> availableLocators = ((SimpleResourceLocatorFactory) locatorFactory).getLocatorFactories();
    final List<?> defaultLocators = new DefaultResourceLocatorFactory().getLocatorFactories();
    Assert.assertTrue(availableLocators.size() > defaultLocators.size());
  }
}
