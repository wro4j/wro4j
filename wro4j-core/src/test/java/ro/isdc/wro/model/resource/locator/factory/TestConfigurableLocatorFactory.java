package ro.isdc.wro.model.resource.locator.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * @author Alex Objelean
 */
public class TestConfigurableLocatorFactory {
  @Mock
  private ResourceLocatorFactory mockResourceLocatorFactory;
  @Mock
  private ResourceLocator mockResourceLocator;
  @Mock
  private ProviderFinder<LocatorProvider> mockProviderFinder;
  private ConfigurableLocatorFactory victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ConfigurableLocatorFactory();
  }
  
  @Test
  public void shouldHaveEmptyConfiguredStrategiesByDefault() {
    assertTrue(victim.getConfiguredStrategies().isEmpty());
  }
  
  @Test
  public void shouldHaveNonEmptyListOfAvailableStrategies() {
    assertEquals(5, victim.getAvailableStrategies().size());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotSetInvalidLocatorAlias() {
    final Properties props = createPropsWithLocators("invalid");
    victim.setProperties(props);
    victim.getConfiguredStrategies();
  }
  
  private Properties createPropsWithLocators(final String locatorsAsString) {
    final Properties props = new Properties();
    props.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, locatorsAsString);
    return props;
  }
  
  @Test
  public void shouldDetectConfiguredLocator() {
    final String locatorsAsString = ServletContextResourceLocator.ALIAS_DISPATCHER_FIRST;
    victim.setProperties(createPropsWithLocators(locatorsAsString));
    
    final List<ResourceLocatorFactory> locators = victim.getConfiguredStrategies();

    assertEquals(1, locators.size());
    assertEquals(ServletContextResourceLocator.class, locators.iterator().next().getClass());
  }
  
  @Test
  public void shouldDetectConfiguredLocators() {
    final String locatorsAsString = ConfigurableLocatorFactory.createItemsAsString(
        ServletContextResourceLocator.ALIAS_DISPATCHER_FIRST,
        ServletContextResourceLocator.ALIAS_SERVLET_CONTEXT_FIRST, ServletContextResourceLocator.ALIAS,
        ClasspathResourceLocator.ALIAS, UrlResourceLocator.ALIAS);
    victim.setProperties(createPropsWithLocators(locatorsAsString));
    
    final List<ResourceLocatorFactory> locatorFactories = victim.getConfiguredStrategies();
    
    assertEquals(5, locatorFactories.size());
    
    final Iterator<ResourceLocatorFactory> iterator = locatorFactories.iterator();
    assertEquals(ServletContextResourceLocator.class, iterator.next().getClass());
    assertEquals(ServletContextResourceLocator.class, iterator.next().getClass());
    assertEquals(ServletContextResourceLocator.class, iterator.next().getClass());
    assertEquals(ClasspathResourceLocator.class, iterator.next().getClass());
    assertEquals(UrlResourceLocator.class, iterator.next().getClass());
  }
  
  @Test
  public void shouldUseDefaultLocatorWhenNoneIsConfigured() {
    final ResourceLocator locator = victim.getInstance("/");
    assertEquals(ServletContextResourceLocator.class, locator.getClass());
  }
  
  @Test
  public void shouldOverrideAvailableLocator() {
    victim = new ConfigurableLocatorFactory() {
      @Override
      protected void overrideDefaultStrategyMap(final Map<String, ResourceLocatorFactory> map) {
        map.clear();
        map.put(ServletContextResourceLocator.ALIAS, mockResourceLocatorFactory);
      }
    };
    final String locatorsAsString = ConfigurableLocatorFactory.createItemsAsString(ServletContextResourceLocator.ALIAS);
    victim.setProperties(createPropsWithLocators(locatorsAsString));
    final List<ResourceLocatorFactory> locators = victim.getConfiguredStrategies();
    assertEquals(1, locators.size());
    
    final Iterator<ResourceLocatorFactory> iterator = locators.iterator();
    assertSame(mockResourceLocator, iterator.next());
  }

  @Test
  public void shouldNotFailWhenASingleProviderFails() {
    victim = new ConfigurableLocatorFactory() {
      @Override
      protected ProviderFinder<LocatorProvider> getProviderFinder() {
        return mockProviderFinder;
      }
    };
    final List<LocatorProvider> providers = new ArrayList<LocatorProvider>();
    providers.add(new LocatorProvider() {
      public Map<String, ResourceLocatorFactory> provideLocators() {
        throw new IllegalStateException("Unexpected BOOM!");
      }
    });
    when(mockProviderFinder.find()).thenReturn(providers);
    assertTrue(victim.getAvailableStrategies().isEmpty());
  }
  
  @Test
  public void shouldComputeCorrectlyAvailableStrategiesDependingOnProviderFinder() {
    victim = new ConfigurableLocatorFactory() {
      @Override
      protected ProviderFinder<LocatorProvider> getProviderFinder() {
        return mockProviderFinder;
      }
    };
    when(mockProviderFinder.find()).thenReturn(new ArrayList<LocatorProvider>());
    assertTrue(victim.getAvailableStrategies().isEmpty());
    
    final List<LocatorProvider> providers = new ArrayList<LocatorProvider>();
    providers.add(new LocatorProvider() {
      public Map<String, ResourceLocatorFactory> provideLocators() {
        final Map<String, ResourceLocatorFactory> map = new HashMap<String, ResourceLocatorFactory>();
        map.put("first", mockResourceLocatorFactory);
        map.put("second", mockResourceLocatorFactory);
        return map;
      }
    });
    victim = new ConfigurableLocatorFactory() {
      @Override
      protected ProviderFinder<LocatorProvider> getProviderFinder() {
        return mockProviderFinder;
      }
    };
    when(mockProviderFinder.find()).thenReturn(providers);
    assertEquals(2, victim.getAvailableStrategies().size());
    assertEquals("[second, first]", victim.getAvailableAliases().toString());
  }
}
