/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * TestConfigurableWroManagerFactory.
 * 
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory factory;
  @Mock
  private FilterConfig mockFilterConfig;
  private SimpleUriLocatorFactory uriLocatorFactory;
  @Mock
  private ServletContext mockServletContext;
  private ProcessorsFactory processorsFactory;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  
  public void initFactory(final FilterConfig filterConfig) {
    Context.set(Context.webContext(mockRequest, mockResponse, filterConfig));
    
    factory = new ConfigurableWroManagerFactory();
    // create one instance for test
    final WroManager manager = factory.create();
    processorsFactory = manager.getProcessorsFactory();
    uriLocatorFactory = (SimpleUriLocatorFactory) AbstractDecorator.getOriginalDecoratedObject(manager.getUriLocatorFactory());
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
  
  /**
   * When no uri locators are set, the default factory is used.
   */
  @Test
  public void testWhenNoUriLocatorsParamSet() {
    initFactory(mockFilterConfig);
    factory.create();
    Assert.assertFalse(uriLocatorFactory.getUriLocators().isEmpty());
  }
  
  @Test
  public void testWithEmptyUriLocators() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertFalse(uriLocatorFactory.getUriLocators().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidUriLocatorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn(
        "INVALID1,INVALID2");
    initFactory(filterConfig);
    uriLocatorFactory.getUriLocators();
  }
  
  @Test
  public void testWhenValidLocatorsSet() {
    configureValidUriLocators(mockFilterConfig);
    Assert.assertEquals(3, uriLocatorFactory.getUriLocators().size());
  }
  
  /**
   * @param filterConfig
   */
  private void configureValidUriLocators(final FilterConfig filterConfig) {
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn(
        "servletContext, url, classpath");
    initFactory(filterConfig);
  }
  
  @Test
  public void testProcessorsExecutionOrder() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        JSMinProcessor.ALIAS + "," + CssImportPreProcessor.ALIAS + "," + CssVariablesProcessor.ALIAS);
    initFactory(mockFilterConfig);
    final List<ResourcePreProcessor> list = (List<ResourcePreProcessor>) processorsFactory.getPreProcessors();
    Assert.assertEquals(JSMinProcessor.class, list.get(0).getClass());
    Assert.assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    Assert.assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }
  
  @Test
  public void testWithEmptyPreProcessors() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertTrue(processorsFactory.getPreProcessors().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    initFactory(filterConfig);
    processorsFactory.getPreProcessors();
  }
  
  @Test
  public void testWhenValidPreProcessorsSet() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "cssUrlRewriting");
    initFactory(mockFilterConfig);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
  }
  
  @Test
  public void testWithEmptyPostProcessors() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertTrue(processorsFactory.getPostProcessors().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    initFactory(mockFilterConfig);
    processorsFactory.getPostProcessors();
  }
  
  @Test
  public void testWhenValidPostProcessorsSet() {
    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
        "cssMinJawr, jsMin, cssVariables");
    initFactory(mockFilterConfig);
    Assert.assertEquals(3, processorsFactory.getPostProcessors().size());
  }
  
  @Test
  public void testConfigPropertiesWithValidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "cssMin");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    Collection<ResourcePreProcessor> list = processorsFactory.getPreProcessors();
    Assert.assertEquals(1, list.size());
    Assert.assertEquals(CssMinProcessor.class, list.iterator().next().getClass());
  }
  
  @Test
  public void testConfigPropertiesWithValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(1, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(
        JSMinProcessor.class,
        ((ProcessorDecorator) processorsFactory.getPostProcessors().iterator().next()).getDecoratedObject().getClass());
  }
  
  @Test
  public void testConfigPropertiesWithMultipleValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin, cssMin");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(2, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(
        JSMinProcessor.class,
        ((ProcessorDecorator) processorsFactory.getPostProcessors().iterator().next()).getDecoratedObject().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "INVALID");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    processorsFactory.getPreProcessors();
  }
  
  public void shouldUseExtensionAwareProcessorWhenProcessorNameContainsDotCharacter() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "jsMin.js");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
    Assert.assertTrue(processorsFactory.getPreProcessors().iterator().next() instanceof ExtensionsAwareProcessorDecorator);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "INVALID");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    processorsFactory.getPostProcessors();
  }
}
