/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.support.OrderedProcessorProvider;
import ro.isdc.wro.model.resource.processor.support.UnorderedProcessorProvider;


/**
 * @author Alex Objelean
 */
public class TestConfigurableProcessorsFactory {
  private ConfigurableProcessorsFactory victim;

  @Before
  public void setUp() {
    initMocks(this);
    victim = new ConfigurableProcessorsFactory();
  }

  @Test
  public void shouldReturnEmptyListOfProcessors() {
    assertEquals(Collections.EMPTY_LIST, victim.getPreProcessors());
    assertEquals(Collections.EMPTY_LIST, victim.getPostProcessors());
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidPreProcessorSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "invalid");
    victim.setProperties(props);
    victim.getPreProcessors();
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidPostProcessorSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "invalid");
    victim.setProperties(props);
    victim.getPostProcessors();
  }

  @Test
  public void testGetValidPreProcessorSet() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put("valid", Mockito.mock(ResourcePreProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "valid");
    victim.setPreProcessorsMap(map);
    victim.setProperties(props);
    assertEquals(1, victim.getPreProcessors().size());
  }

  @Test
  public void testGetValidPostProcessorSet() {
    final Map<String, ResourcePostProcessor> map = new HashMap<String, ResourcePostProcessor>();
    map.put("valid", Mockito.mock(ResourcePostProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "valid");
    victim.setPostProcessorsMap(map);
    victim.setProperties(props);
    assertEquals(1, victim.getPostProcessors().size());
  }

  @Test
  public void cannotAcceptExtensionAwareConfigurationForPostProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    final String extension = "js";
    final String processorName = "valid";
    map.put(processorName, Mockito.mock(ResourcePreProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS,
        String.format("%s.%s", processorName, extension));
    victim.setPreProcessorsMap(map);
    victim.setProperties(props);
    assertEquals(0, victim.getPreProcessors().size());
  }

  @Test
  public void shouldDecorateWithExtensionAwareProcessorDecorator() {
    genericShouldDecorateWithExtension("valid", "js");
  }

  @Test
  public void shouldDecorateWithExtensionAwareProcessorDecoratorWhenProcessorNameContainsDots() {
    genericShouldDecorateWithExtension("valid.processor.name", "js");
  }

  private void genericShouldDecorateWithExtension(final String processorName, final String extension) {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(processorName, Mockito.mock(ResourcePreProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS,
        String.format("%s.%s", processorName, extension));
    victim.setPreProcessorsMap(map);
    victim.setProperties(props);
    assertEquals(1, victim.getPreProcessors().size());
    assertTrue(victim.getPreProcessors().iterator().next() instanceof ExtensionsAwareProcessorDecorator);
  }

  @Test
  public void unorderedShouldOverrideDefault() {
    final  Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, ConformColorsCssProcessor.ALIAS);
    victim.setProperties(props);
    assertSame(victim.getPreProcessors().iterator().next(), UnorderedProcessorProvider.CONFORM_COLORS);
  }

  @Test
  public void orderedShouldOverrideUnordered() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, OrderedProcessorProvider.ALIAS);
    victim.setProperties(props);
    System.out.println(props);
    System.out.println(victim.getPreProcessors());
    assertSame(victim.getPreProcessors().iterator().next(), OrderedProcessorProvider.CUSTOM);
  }
}
