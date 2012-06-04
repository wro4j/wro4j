/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.processor.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import ro.isdc.wro.model.resource.processor.ProcessorsProvider;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;

/**
 * @author Alex Objelean
 */
public class TestConfigurableProcessorsFactory {
  @Mock
  private ResourceProcessor mockPreProcessor;
  @Mock
  private ResourceProcessor mockPostProcessor;
  
  private ConfigurableProcessorsFactory factory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
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
    factory = new ConfigurableProcessorsFactory() {
      @Override
      Iterator<ProcessorsProvider> lookupProviders() {
        throw new IllegalStateException("BOOM!");
      }
    };
    factory.getPreProcessors();
  }
  
  @Test
  public void shouldNotFailWhenASingleProviderFails() {
    factory = new ConfigurableProcessorsFactory() {
      @Override
      Iterator<ProcessorsProvider> lookupProviders() {
        final List<ProcessorsProvider> list = new ArrayList<ProcessorsProvider>();
        list.add(new ProcessorsProvider() {
          public Map<String, ResourceProcessor> providePreProcessors() {
            throw new IllegalStateException("BOOM!");
          }
          
          public Map<String, ResourceProcessor> providePostProcessors() {
            throw new IllegalStateException("BOOM!");
          }
        });
        list.add(new ProcessorsProvider() {
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
        return list.iterator();
      }
    };
    Assert.assertEquals(2, factory.getAvailablePreProcessors().size());
    Assert.assertEquals(3, factory.getAvailablePostProcessors().size());
  } 
}
