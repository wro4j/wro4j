package ro.isdc.wro.util.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

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
  public void shouldFindConfigurableProviders() {
    victim = ProviderFinder.of(ConfigurableProvider.class);
    assertEquals(2, victim.find().size());
  }

  @Test
  public void shouldFindProcessorsProvider() {
    victim = ProviderFinder.of(ProcessorProvider.class);
    assertEquals(4, victim.find().size());
  }

  @Test
  public void shouldFindNamingStrategyProviders() {
    victim = ProviderFinder.of(NamingStrategyProvider.class);
    assertEquals(2, victim.find().size());
  }

  @Test
  public void shouldFindHashBuilderProviders() {
    victim = ProviderFinder.of(HashStrategyProvider.class);
    assertEquals(2, victim.find().size());
  }

  @Test
  public void shouldNotFindProviderWhenNoneIsAvailable() {
    victim = new ProviderFinder<ProcessorProvider>(ProcessorProvider.class) {
      @Override
      <F> Iterator<F> lookupProviders(final Class<F> clazz) {
        return new ArrayList<F>().iterator();
      }
    };
    assertTrue(victim.find().isEmpty());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotFindAnyProviderWhenLookupFails() {
    victim = new ProviderFinder<ProcessorProvider>(ProcessorProvider.class) {
      @Override
      <F> Iterator<F> lookupProviders(final Class<F> clazz) {
        throw new IllegalStateException("BOOM!");
      }
    };
    victim.find();
  }
}
