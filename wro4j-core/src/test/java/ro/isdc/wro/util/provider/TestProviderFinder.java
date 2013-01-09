package ro.isdc.wro.util.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;
import ro.isdc.wro.util.Ordered;

/**
 * @author Alex Objelean
 */
public class TestProviderFinder {
  private ProviderFinder<?> victim;

  @Test
  public void shouldFindProcessorsProvider() {
    victim = ProviderFinder.of(ProcessorProvider.class);
    Assert.assertFalse(victim.find().isEmpty());
  }

  @Test
  public void shouldFindNamingStrategyProviders() {
    victim = ProviderFinder.of(NamingStrategyProvider.class);
    Assert.assertFalse(victim.find().isEmpty());
  }

  @Test
  public void shouldFindHashBuilderProviders() {
    victim = ProviderFinder.of(HashStrategyProvider.class);
    Assert.assertFalse(victim.find().isEmpty());
  }

  @Test
  public void shouldNotFindProviderWhenNoneIsAvailable() {
    victim = new ProviderFinder<ProcessorProvider>(ProcessorProvider.class) {
      @Override
      <F> Iterator<F> lookupProviders(final Class<F> clazz) {
        return new ArrayList<F>().iterator();
      }
    };
    Assert.assertTrue(victim.find().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotFindAnyProviderWhenLookupFails() {
    victim = new ProviderFinder<ProcessorProvider>(ProcessorProvider.class) {
      @Override
      <F> Iterator<F> lookupProviders(final Class<F> clazz) {
        throw new IllegalStateException("BOOM!");
      }
    };
    victim.find();
  }

  @Test
  public void shouldFindProcessorProviders() {
    Assert.assertNotNull(ProviderFinder.of(ProcessorProvider.class).find());
  }
  
  @Test
  public void shouldOrderProviders() {
    final PrioritizableProvider lowest = new PrioritizableProvider(Ordered.LOWEST);
    final Object defaultPriority = new Object();
    final PrioritizableProvider highest = new PrioritizableProvider(Ordered.HIGHEST);
    
    victim = new ProviderFinder<Object>(Object.class) {
      @Override @SuppressWarnings("unchecked")
      <P> Iterator<P> lookupProviders(Class<P> providerClass) {
        if (providerClass == Object.class) {
          return (Iterator<P>) Arrays.asList(defaultPriority, highest, lowest).iterator();
        }
        
        return Collections.<P>emptyList().iterator();
      }
    };
    
    Assert.assertEquals(Arrays.asList(lowest, defaultPriority, highest), victim.find());
  }

  
  private static class PrioritizableProvider implements Ordered {
    private final int providerPriority;
    
    public PrioritizableProvider(int providerPriority) {
      this.providerPriority = providerPriority;
    }

    public int getOrder() {
      return providerPriority;
    }
  }
}
