package ro.isdc.wro.util.provider;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;

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
}
