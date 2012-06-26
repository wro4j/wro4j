package ro.isdc.wro.model.resource.locator.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;


/**
 * @author Alex Objelean
 */
public class TestConfigurableLocatorFactory {
  @Mock
  private UriLocator mockUriLocator;
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
    final String locatorsAsString = ServletContextUriLocator.ALIAS_DISPATCHER_FIRST;
    victim.setProperties(createPropsWithLocators(locatorsAsString));
    
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
}
