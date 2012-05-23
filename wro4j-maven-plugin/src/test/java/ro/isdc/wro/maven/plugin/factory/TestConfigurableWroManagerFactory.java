package ro.isdc.wro.maven.plugin.factory;

import static ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS;
import static ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestConfigurableWroManagerFactory {
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  private ProcessorsFactory processorsFactory;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  
  public void initFactory(final FilterConfig filterConfig) {
    initFactory(filterConfig, new Properties());
  }
  
  public void initFactory(final FilterConfig filterConfig, final Properties properties) {
    Validate.notNull(properties);
    Context.set(Context.webContext(mockRequest, mockResponse, filterConfig));
    
    final ConfigurableWroManagerFactory factory = new ConfigurableWroManagerFactory() {
      @Override
      protected Properties createProperties() {
        return properties;
      }
      @Override
      protected WroModelFactory newModelFactory() {
        return WroTestUtils.simpleModelFactory(new WroModel());
      }
    };
    factory.initialize(new StandaloneContext());
    // create one instance for test
    final WroManager manager = factory.create();
    processorsFactory = manager.getProcessorsFactory();
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    // init context
    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testProcessorsExecutionOrder() {
    final Properties props = createProperties(PARAM_PRE_PROCESSORS, JSMinProcessor.ALIAS + ","
        + CssImportPreProcessor.ALIAS + "," + CssVariablesProcessor.ALIAS);
    initFactory(mockFilterConfig, props);
    
    final Collection<ResourceProcessor> list = processorsFactory.getPreProcessors();
    
    Assert.assertFalse(list.isEmpty());
    Iterator<ResourceProcessor> iterator = list.iterator();
    Assert.assertEquals(JSMinProcessor.class, iterator.next().getClass());
    Assert.assertEquals(CssImportPreProcessor.class, iterator.next().getClass());
    Assert.assertEquals(CssVariablesProcessor.class, iterator.next().getClass());
  }

  private Properties createProperties(final String key, final String value) {
    Properties props = new Properties();
    props.setProperty(key, value);
    return props;
  }
  
  @Test
  public void testWithEmptyPreProcessors() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertTrue(processorsFactory.getPreProcessors().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    initFactory(mockFilterConfig, createProperties(PARAM_PRE_PROCESSORS, "INVALID1,INVALID2"));
    processorsFactory.getPreProcessors();
  }
  
  @Test
  public void testWhenValidPreProcessorsSet() {
    initFactory(mockFilterConfig, createProperties(PARAM_PRE_PROCESSORS, "cssUrlRewriting"));
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
  }
  
  @Test
  public void testWithEmptyPostProcessors() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertTrue(processorsFactory.getPostProcessors().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    initFactory(mockFilterConfig, createProperties(PARAM_POST_PROCESSORS, "INVALID1,INVALID2"));
    processorsFactory.getPostProcessors();
  }
  
  @Test
  public void testWhenValidPostProcessorsSet() {
    initFactory(mockFilterConfig, createProperties(PARAM_POST_PROCESSORS, "cssMinJawr, jsMin, cssVariables"));
    Assert.assertEquals(3, processorsFactory.getPostProcessors().size());
  }
  
  @Test
  public void testConfigPropertiesWithValidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "cssMin");
    initFactory(mockFilterConfig, configProperties);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
    Assert.assertEquals(CssMinProcessor.class,
        processorsFactory.getPreProcessors().iterator().next().getClass());
  }
  
  @Test
  public void testConfigPropertiesWithValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin");
    initFactory(mockFilterConfig, configProperties);
    Assert.assertEquals(1, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(JSMinProcessor.class, processorsFactory.getPostProcessors().iterator().next().getClass());
  }
  
  @Test
  public void testConfigPropertiesWithMultipleValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin, cssMin");
    initFactory(mockFilterConfig, configProperties);
    Assert.assertEquals(2, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(JSMinProcessor.class, processorsFactory.getPostProcessors().iterator().next().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "INVALID");
    initFactory(mockFilterConfig, configProperties);
    processorsFactory.getPreProcessors();
  }
  
  public void shouldUseExtensionAwareProcessorWhenProcessorNameContainsDotCharacter() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "jsMin.js");
    initFactory(mockFilterConfig, configProperties);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
    Assert.assertTrue(processorsFactory.getPreProcessors().iterator().next() instanceof ExtensionsAwareProcessorDecorator);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "INVALID");
    initFactory(mockFilterConfig, configProperties);
    processorsFactory.getPostProcessors();
  }

}
