package ro.isdc.wro.model.resource.support;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.Ordered;
import ro.isdc.wro.util.provider.ProviderFinder;

public class TestAbstractConfigurableStrategySupport {

  @Test
  public void shouldOverrideLowPriorityProvidersWithHigherPriorityProviders() {
    final UriLocator preferredUriLocator = new ClasspathUriLocator();
    
    AbstractConfigurableStrategySupport<UriLocator, LocatorProvider> strategySupport = new AbstractConfigurableStrategySupport<UriLocator, LocatorProvider>() {
      @Override
      protected ProviderFinder<LocatorProvider> getProviderFinder() {
        LocatorProvider lowPriorityProvider = new PrioritizableLocatorProvider(new UrlUriLocator(), Ordered.LOWEST);
        LocatorProvider highPriorityProvider = new PrioritizableLocatorProvider(preferredUriLocator, Ordered.HIGHEST);

        @SuppressWarnings("unchecked")
        ProviderFinder<LocatorProvider> providerFinder = mock(ProviderFinder.class);
        when(providerFinder.find()).thenReturn(asList(highPriorityProvider, lowPriorityProvider));

        return providerFinder;
      }
      
      @Override
      protected Map<String, UriLocator> getStrategies(LocatorProvider provider) {
        return provider.provideLocators();
      }
      
      @Override
      protected String getStrategyKey() {
        return null;
      }
    };
    
    UriLocator selectedUriLocator = strategySupport.getStrategyForAlias(UrlUriLocator.ALIAS);
    
    assertSame(preferredUriLocator, selectedUriLocator);
  }
  
  private static class PrioritizableLocatorProvider implements LocatorProvider, Ordered {
    private final UriLocator uriLocator;
    private final int providerPriority;
    
    public PrioritizableLocatorProvider(UriLocator uriLocator, int providerPriority) {
      this.uriLocator = uriLocator;
      this.providerPriority = providerPriority;
    }

    public int getOrder() {
      return providerPriority;
    }

    public Map<String, UriLocator> provideLocators() {
      HashMap<String, UriLocator> locators = new HashMap<String, UriLocator>();
      locators.put(UrlUriLocator.ALIAS, uriLocator);
      return locators;
    }
  }
}
