/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * TestConfigurableWroManagerFactory.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory factory;
  private FilterConfig filterConfig;
  private ProcessorsFactory processorsFactory;


  public void initFactory(final FilterConfig filterConfig) {
    // init context
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Context.set(Context.webContext(request, response, filterConfig));

    factory = new ConfigurableWroManagerFactory() {
      @Override
      protected ProcessorsFactory newProcessorsFactory() {
        return processorsFactory = super.newProcessorsFactory();
      };
    };
    // create one instance for test
    factory.create();
  }


  @Before
  public void setUp() {
    filterConfig = Mockito.mock(FilterConfig.class);
  }


  @After
  public void tearDown() {
    Context.unset();
  }


  @Test
  public void testProcessorsExecutionOrder() {
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
      JSMinProcessor.ALIAS + "," + CssImportPreProcessor.ALIAS + "," + CssVariablesProcessor.ALIAS);
    initFactory(filterConfig);
    final List<ResourceProcessor> list = (List<ResourceProcessor>)processorsFactory.getPreProcessors();
    Assert.assertEquals(JSMinProcessor.class, list.get(0).getClass());
    Assert.assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    Assert.assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }


  @Test
  public void testWithEmptyPreProcessors() {
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
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
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
      "cssUrlRewriting");
    initFactory(filterConfig);
    Assert.assertEquals(1, processorsFactory.getPreProcessors().size());
  }


  @Test
  public void testWithEmptyPostProcessors() {
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    Assert.assertTrue(processorsFactory.getPostProcessors().isEmpty());
  }


  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
      "INVALID1,INVALID2");
    initFactory(filterConfig);
    processorsFactory.getPostProcessors();
  }


  @Test
  public void testWhenValidPostProcessorsSet() {
    Mockito.when(filterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
      "cssMinJawr, jsMin, cssVariables");
    initFactory(filterConfig);
    Assert.assertEquals(3, processorsFactory.getPostProcessors().size());
  }
}
