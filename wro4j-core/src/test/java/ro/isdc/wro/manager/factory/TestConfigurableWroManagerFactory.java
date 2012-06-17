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
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.support.hash.ConfigurableHashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.MD5HashStrategy;
import ro.isdc.wro.model.resource.support.naming.ConfigurableNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.TimestampNamingStrategy;
import ro.isdc.wro.util.WroUtil;


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
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        JSMinProcessor.ALIAS + "," + CssImportPreProcessor.ALIAS + "," + CssVariablesProcessor.ALIAS);
    initFactory(mockFilterConfig);
    final List<ResourceProcessor> list = (List<ResourceProcessor>) processorsFactory.getPreProcessors();
    Assert.assertEquals(JSMinProcessor.class, list.get(0).getClass());
    Assert.assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    Assert.assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }
  
  @Test
  public void testWithEmptyPreProcessors() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(mockFilterConfig);
    Assert.assertTrue(processorsFactory.getPreProcessors().isEmpty());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    initFactory(filterConfig);
    processorsFactory.getPreProcessors();
  }
  
  @Test
  public void testWhenValidPreProcessorsSet() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "cssUrlRewriting");
    initFactory(mockFilterConfig);
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
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    initFactory(mockFilterConfig);
    processorsFactory.getPostProcessors();
  }
  
  @Test
  public void testWhenValidPostProcessorsSet() {
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
    Collection<ResourceProcessor> list = processorsFactory.getPreProcessors();
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
    Assert.assertEquals(JSMinProcessor.class, processorsFactory.getPostProcessors().iterator().next().getClass());
  }
  
  @Test
  public void testConfigPropertiesWithMultipleValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin, cssMin");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(2, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(JSMinProcessor.class, processorsFactory.getPostProcessors().iterator().next().getClass());
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
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidNamingStrategy() throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableNamingStrategy.KEY, "INVALID");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    factory.create().getNamingStrategy().rename("name", WroUtil.EMPTY_STREAM);
  }
  
  @Test
  public void shouldUseConfiguredNamingStrategy() throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableNamingStrategy.KEY, TimestampNamingStrategy.ALIAS);
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    final NamingStrategy actual = ((ConfigurableNamingStrategy) factory.create().getNamingStrategy()).getConfiguredStrategy();
    Assert.assertEquals(TimestampNamingStrategy.class, actual.getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidHashStrategy() throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableHashStrategy.KEY, "INVALID");
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    factory.create().getHashStrategy().getHash(WroUtil.EMPTY_STREAM);
  }
  
  @Test
  public void shouldUseConfiguredHashStrategy() throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableHashStrategy.KEY, MD5HashStrategy.ALIAS);
    initFactory(mockFilterConfig);
    factory.setConfigProperties(configProperties);
    final HashStrategy actual = ((ConfigurableHashStrategy) factory.create().getHashStrategy()).getConfiguredStrategy();
    Assert.assertEquals(MD5HashStrategy.class, actual.getClass());
  }
}
