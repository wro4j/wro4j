/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ProcessorProvider;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * @author Alex Objelean
 */
public class TestConfigurableProcessorsFactory {
  @Mock
  private ResourceProcessor mockPreProcessor;
  @Mock
  private ResourceProcessor mockPostProcessor;
  @Mock
  private ProviderFinder<ProcessorProvider> mockProviderFinder;
  private ConfigurableProcessorsFactory factory;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    factory = new ConfigurableProcessorsFactory() {
      @Override
      ProviderFinder<ProcessorProvider> getProcessorProviderFinder() {
        return mockProviderFinder;
      }
    };
  }
  
  @Test
  public void shouldReturnEmptyListOfProcessors() {
    assertEquals(Collections.EMPTY_LIST, factory.getPreProcessors());
    assertEquals(Collections.EMPTY_LIST, factory.getPostProcessors());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testInvalidPreProcessorSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "invalid");
    factory.setProperties(props);
    factory.getPreProcessors();
  }
  
  @Test(expected = WroRuntimeException.class)
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
  public void cannotAcceptExtensionAwareConfigurationForPostProcessors() {
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
  public void shouldDecorateWithExtensionAwareProcessorDecorator() {
    genericShouldDecorateWithExtension("valid", "js");
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
  
  @Test(expected = WroRuntimeException.class)
  public void cannotContinueWhenDiscoveryOfProcessorsFails() {
    Mockito.when(mockProviderFinder.find()).thenThrow(new WroRuntimeException("BOOM!"));
    factory.getPreProcessors();
  }
  
  @Test
  public void shouldNotFailWhenASingleProviderFails() {
    factory = new ConfigurableProcessorsFactory() {
      @Override
      ProviderFinder<ProcessorProvider> getProcessorProviderFinder() {
        final List<ProcessorProvider> list = new ArrayList<ProcessorProvider>();
        list.add(new ProcessorProvider() {
          public Map<String, ResourceProcessor> providePreProcessors() {
            throw new IllegalStateException("BOOM!");
          }
          
          public Map<String, ResourceProcessor> providePostProcessors() {
            throw new IllegalStateException("BOOM!");
          }
        });
        list.add(new ProcessorProvider() {
          public Map<String, ResourceProcessor> providePreProcessors() {
            final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
            map.put("p1", mockPreProcessor);
            map.put("p2", mockPreProcessor);
            return map;
          }
          
          public Map<String, ResourceProcessor> providePostProcessors() {
            final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
            map.put("p1", mockPostProcessor);
            map.put("p2", mockPostProcessor);
            map.put("p3", mockPostProcessor);
            return map;
          }
        });
        Mockito.when(mockProviderFinder.find()).thenReturn(list);
        return mockProviderFinder;
      }
    };
    Assert.assertEquals(2, factory.getAvailablePreProcessors().size());
    Assert.assertEquals(3, factory.getAvailablePostProcessors().size());
  }
}
