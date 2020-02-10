package ro.isdc.wro.model.resource.locator.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * @author Alex Objelean
 */
public class TestConfigurableLocatorFactory {
  @Mock
  private UriLocator mockUriLocator;
  @Mock
  private ProviderFinder<LocatorProvider> mockProviderFinder;
  private ConfigurableLocatorFactory victim;
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
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
    assertEquals(8, victim.getAvailableStrategies().size());
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
    victim.setProperties(createPropsWithLocators(ServletContextUriLocator.ALIAS_DISPATCHER_FIRST));
    final List<UriLocator> locators = victim.getConfiguredStrategies();

    assertEquals(1, locators.size());
    assertEquals(ServletContextUriLocator.class, locators.iterator().next().getClass());
  }

  @Test
  public void shouldUseServletContextOnlyLocator() {
    victim.setProperties(createPropsWithLocators(ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_ONLY));
    final List<UriLocator> locators = victim.getConfiguredStrategies();

    assertEquals(1, locators.size());
    assertEquals(ServletContextUriLocator.class, locators.iterator().next().getClass());
  }

  @Test
  public void shouldDetectConfiguredLocators() {
    final String locatorsAsString = ConfigurableLocatorFactory.createItemsAsString(
        ServletContextUriLocator.ALIAS_DISPATCHER_FIRST, ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_FIRST,
        ServletContextUriLocator.ALIAS, ClasspathUriLocator.ALIAS, UrlUriLocator.ALIAS);
    victim.setProperties(createPropsWithLocators(locatorsAsString));

    final List<UriLocator> locators = victim.getConfiguredStrategies();

    assertEquals(5, locators.size());

    final Iterator<UriLocator> iterator = locators.iterator();
    assertEquals(ServletContextUriLocator.class, iterator.next().getClass());
    assertEquals(ServletContextUriLocator.class, iterator.next().getClass());
    assertEquals(ServletContextUriLocator.class, iterator.next().getClass());
    assertEquals(ClasspathUriLocator.class, iterator.next().getClass());
    assertEquals(UrlUriLocator.class, iterator.next().getClass());
  }

  @Test
  public void shouldUseDefaultLocatorWhenNoneIsConfigured() {
    final UriLocator locator = victim.getInstance("/");
    assertEquals(ServletContextUriLocator.class, locator.getClass());
  }

  @Test
  public void shouldOverrideAvailableLocator() {
    victim = new ConfigurableLocatorFactory() {
      @Override
      protected void overrideDefaultStrategyMap(final Map<String, UriLocator> map) {
        map.clear();
        map.put(ServletContextUriLocator.ALIAS, mockUriLocator);
      }
    };
    final String locatorsAsString = ConfigurableLocatorFactory.createItemsAsString(ServletContextUriLocator.ALIAS);
    victim.setProperties(createPropsWithLocators(locatorsAsString));
    final List<UriLocator> locators = victim.getConfiguredStrategies();
    assertEquals(1, locators.size());

    final Iterator<UriLocator> iterator = locators.iterator();
    assertSame(mockUriLocator, iterator.next());
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
      public Map<String, UriLocator> provideLocators() {
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
      public Map<String, UriLocator> provideLocators() {
        final Map<String, UriLocator> map = new LinkedHashMap<String, UriLocator>();
        map.put("first", mockUriLocator);
        map.put("second", mockUriLocator);
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
    assertEquals("[first, second]", victim.getAvailableAliases().toString());
  }
}
