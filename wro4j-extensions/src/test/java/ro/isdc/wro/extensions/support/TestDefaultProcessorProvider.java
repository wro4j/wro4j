package ro.isdc.wro.extensions.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.support.DefaultProcessorProvider;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * @author Alex Objelean
 */
public class TestDefaultProcessorProvider {
  private DefaultProcessorProvider victim;

  @Before
  public void setUp() {
    victim = new DefaultProcessorProvider();
  }

  @Test
  public void shouldLoadNonEmptyPostProcessors()
      throws Exception {
    final Map<String, ResourcePostProcessor> map = victim.providePostProcessors();
    assertFalse(map.isEmpty());
  }

  @Test
  public void shouldLoadNonEmptyPreProcessors()
      throws Exception {
    final Map<String, ResourcePreProcessor> map = victim.providePreProcessors();
    assertFalse(map.isEmpty());
  }

  @Test
  public void shouldProvideGoogleClosureWhitespace()
      throws Exception {
    assertTrue(victim.providePreProcessors().keySet().contains(GoogleClosureCompressorProcessor.ALIAS_WHITESPACE_ONLY));
  }
}
