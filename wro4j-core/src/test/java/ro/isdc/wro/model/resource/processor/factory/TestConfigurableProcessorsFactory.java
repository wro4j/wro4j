/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.ExtensionsAwareProcessorDecorator;

/**
 * @author Alex Objelean
 */
public class TestConfigurableProcessorsFactory {
  private ConfigurableProcessorsFactory factory;

  @Before
  public void setUp() {
    factory = new ConfigurableProcessorsFactory();
  }

  @Test
  public void testEmptyProcessors() {
    assertEquals(Collections.EMPTY_LIST, factory.getPreProcessors());
    assertEquals(Collections.EMPTY_LIST, factory.getPostProcessors());
  }

  @Test(expected=WroRuntimeException.class)
  public void testInvalidPreProcessorSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "invalid");
    factory.setProperties(props);
    factory.getPreProcessors();
  }

  @Test(expected=WroRuntimeException.class)
  public void testInvalidPostProcessorSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "invalid");
    factory.setProperties(props);
    factory.getPostProcessors();
  }

  @Test
  public void testGetValidPreProcessorSet() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put("valid", Mockito.mock(ResourceProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "valid");
    factory.setPreProcessorsMap(map);
    factory.setProperties(props);
    assertEquals(1, factory.getPreProcessors().size());
  }

  @Test
  public void testGetValidPostProcessorSet() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put("valid", Mockito.mock(ResourceProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "valid");
    factory.setPostProcessorsMap(map);
    factory.setProperties(props);
    assertEquals(1, factory.getPostProcessors().size());
  }

  @Test
  public void shouldDecorateWithExtensionAwareProcessorDecorator() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    final String extension = "js";
    final String processorName = "valid";
    map.put(processorName, Mockito.mock(ResourceProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS,
        String.format("%s.%s", processorName, extension));
    factory.setPreProcessorsMap(map);
    factory.setProperties(props);
    assertEquals(0, factory.getPreProcessors().size());
  }

  @Test
  public void shouldDecorateWithExtensionAwareProcessorDecoratorWhenProcessorNameContainsDots() {
    genericShouldDecorateWithExtension("valid.processor.name", "js");
  }

  private void genericShouldDecorateWithExtension(final String processorName, final String extension) {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put(processorName, Mockito.mock(ResourceProcessor.class));
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS,
        String.format("%s.%s", processorName, extension));
    factory.setPreProcessorsMap(map);
    factory.setProperties(props);
    assertEquals(1, factory.getPreProcessors().size());
    assertTrue(factory.getPreProcessors().iterator().next() instanceof ExtensionsAwareProcessorDecorator);
  }
}
