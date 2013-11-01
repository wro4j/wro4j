package ro.isdc.wro.util.provider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;

/**
 * @author Alex Objelean
 */
public class TestProviderFinder {
  private ProviderFinder<?> victim;

  @Test
  public void shouldFindConfigurableProviders() {
    victim = ProviderFinder.of(ConfigurableProvider.class);
    assertEquals(3, victim.find().size());
  }

  @Test
  public void shouldFindProcessorsProvider() {
    victim = ProviderFinder.of(ProcessorProvider.class);
    assertEquals(3, victim.find().size());
  }

  @Test
  public void shouldFindNamingStrategyProvider() {
    victim = ProviderFinder.of(NamingStrategyProvider.class);
    System.out.println(victim.find());
    assertEquals(3, victim.find().size());
  }
}
