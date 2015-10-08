package ro.isdc.wro.util.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.hash.HashStrategyProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;
import ro.isdc.wro.util.Ordered;


/**
 * @author Alex Objelean
 */
public class TestProviderFinder {
  private ProviderFinder<?> victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Test
  public void shouldFindConfigurableProviders() {
    victim = ProviderFinder.of(ConfigurableProvider.class);
    assertEquals(1, victim.find().size());
  }

  @Test
  public void shouldFindProcessorsProvider() {
    victim = ProviderFinder.of(ProcessorProvider.class);
    assertEquals(3, victim.find().size());
  }

  @Test
  public void shouldFindNamingStrategyProviders() {
    victim = ProviderFinder.of(NamingStrategyProvider.class);
    assertEquals(1, victim.find().size());
  }

  @Test
  public void shouldFindHashBuilderProviders() {
    victim = ProviderFinder.of(HashStrategyProvider.class);
    assertEquals(1, victim.find().size());
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

  @Test
  public void shouldOrderProviders() {
    final OrderedProvider lowest = new OrderedProvider(Ordered.LOWEST);
    final Object defaultPriority = new Object();
    final OrderedProvider highest = new OrderedProvider(Ordered.HIGHEST);

    victim = new ProviderFinder<Object>(Object.class) {
      @Override
      @SuppressWarnings("unchecked")
      <P> Iterator<P> lookupProviders(final Class<P> providerClass) {
        if (providerClass == Object.class) {
          return (Iterator<P>) Arrays.asList(defaultPriority, highest, lowest).iterator();
        }

        return Collections.<P> emptyList().iterator();
      }
    };
    assertEquals(Arrays.asList(highest, defaultPriority, lowest), victim.find());
  }

  private static class OrderedProvider
      implements Ordered {
    private final int providerPriority;

    public OrderedProvider(final int providerPriority) {
      this.providerPriority = providerPriority;
    }

    public int getOrder() {
      return providerPriority;
    }
  }
}
