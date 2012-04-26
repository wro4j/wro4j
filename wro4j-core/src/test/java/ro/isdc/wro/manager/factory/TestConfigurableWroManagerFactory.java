/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

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
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;

/**
 * TestConfigurableWroManagerFactory.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory factory;
  private FilterConfig filterConfig;
  private SimpleUriLocatorFactory uriLocatorFactory;
  private ProcessorsFactory processorsFactory;

  public void initFactory(final FilterConfig filterConfig) {
    //init context
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Context.set(Context.webContext(request, response, filterConfig));

    factory = new ConfigurableWroManagerFactory() {
      @Override
      protected UriLocatorFactory newUriLocatorFactory() {
        return uriLocatorFactory = (SimpleUriLocatorFactory) super.newUriLocatorFactory();
      };
      @Override
      protected ProcessorsFactory newProcessorsFactory() {
        return processorsFactory = super.newProcessorsFactory();
      };
    };
    //create one instance for test
    factory.create();
  }

  @Before
  public void setUp() {
    filterConfig = Mockito.mock(FilterConfig.class);
    final ServletContext servletContext = Mockito.mock(ServletContext.class);
    Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
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
  	initFactory(filterConfig);
    factory.create();
  	Assert.assertFalse(uriLocatorFactory.getUriLocators().isEmpty());
  }

  @Test
  public void testWithEmptyUriLocators() {
  	Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("");
  	initFactory(filterConfig);
    Assert.assertFalse(uriLocatorFactory.getUriLocators().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidUriLocatorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    uriLocatorFactory.getUriLocators();
  }

  @Test
  public void testWhenValidLocatorsSet() {
    configureValidUriLocators(filterConfig);
    Assert.assertEquals(3, uriLocatorFactory.getUriLocators().size());
  }

  /**
   * @param filterConfig
   */
  private void configureValidUriLocators(final FilterConfig filterConfig) {
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("servletContext, url, classpath");
    initFactory(filterConfig);
  }

  @Test
  public void testProcessorsExecutionOrder() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(JSMinProcessor.ALIAS + "," + CssImportPreProcessor.ALIAS + "," + CssVariablesProcessor.ALIAS);
    initFactory(filterConfig);
    final List<ResourcePreProcessor> list = (List<ResourcePreProcessor>) processorsFactory.getPreProcessors();
    Assert.assertEquals(JSMinProcessor.class, list.get(0).getClass());
    Assert.assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    Assert.assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }

  @Test
  public void testWithEmptyPreProcessors() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    Assert.assertTrue(processorsFactory.getPreProcessors().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    processorsFactory.getPreProcessors();
  }

  @Test
  public void testWhenValidPreProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("cssUrlRewriting");
    initFactory(filterConfig);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
  }

  @Test
  public void testWithEmptyPostProcessors() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    Assert.assertTrue(processorsFactory.getPostProcessors().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    processorsFactory.getPostProcessors();
  }

  @Test
  public void testWhenValidPostProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("cssMinJawr, jsMin, cssVariables");
    initFactory(filterConfig);
    Assert.assertEquals(3, processorsFactory.getPostProcessors().size());
  }

  @Test
  public void testConfigPropertiesWithValidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "cssMin");
    initFactory(filterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
    Assert.assertEquals(CssMinProcessor.class,
      processorsFactory.getPreProcessors().toArray(new ResourcePreProcessor[] {})[0].getClass());
  }

  @Test
  public void testConfigPropertiesWithValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin");
    initFactory(filterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(1, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(JSMinProcessor.class,
      ((ProcessorDecorator)processorsFactory.getPostProcessors().iterator().next()).getDecoratedProcessor().getClass());
  }

  @Test
  public void testConfigPropertiesWithMultipleValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin, cssMin");
    initFactory(filterConfig);
    factory.setConfigProperties(configProperties);
    Assert.assertEquals(2, processorsFactory.getPostProcessors().size());
    Assert.assertEquals(JSMinProcessor.class,
        ((ProcessorDecorator)processorsFactory.getPostProcessors().iterator().next()).getDecoratedProcessor().getClass());
  }


  @Test(expected=WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "INVALID");
    initFactory(filterConfig);
    factory.setConfigProperties(configProperties);
    processorsFactory.getPreProcessors();
  }

  @Test(expected=WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "INVALID");
    initFactory(filterConfig);
    factory.setConfigProperties(configProperties);
    processorsFactory.getPostProcessors();
  }
}
