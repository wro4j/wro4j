package ro.isdc.wro.extensions.support.spi;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.locator.WebjarUriLocator;

/**
 * @author Alex Objelean
 */
public class TestDefaultConfigurableProvider {
  private DefaultConfigurableProvider victim;
  @Before
  public void setUp() {
    victim = new DefaultConfigurableProvider();
  }

  @Test
  public void shouldHavePreProcessors() {
    assertTrue(!victim.providePreProcessors().isEmpty());
  }

  @Test
  public void shouldHavePostProcessors() {
    assertTrue(!victim.providePostProcessors().isEmpty());
  }

  @Test
  public void shouldHaveLocators() {
    assertTrue(!victim.provideLocators().isEmpty());
  }

  @Test
  public void shouldProvideWebjarLocator() {
    assertTrue(victim.provideLocators().containsKey(WebjarUriLocator.ALIAS));
  }
}
