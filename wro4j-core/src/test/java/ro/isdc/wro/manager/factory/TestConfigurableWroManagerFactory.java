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
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssVariablesProcessor;

/**
 * TestConfigurableWroManagerFactory.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory factory;
  private FilterConfig filterConfig;

  public void initFactory(final FilterConfig filterConfig) {
    factory = new ConfigurableWroManagerFactory() {
    	@Override
    	protected void onBeforeCreate() {
    		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    		Context.set(Context.webContext(request, response, filterConfig));
    	}
    };
    //create one instance for test
    factory.getInstance();
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
  public void testWhenNoUriLocatorsParamSet() {
  	initFactory(filterConfig);
    factory.getInstance();
  	Assert.assertTrue(factory.getLocators().isEmpty());
  }

  @Test
  public void testWithEmptyUriLocators() {
  	Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("");
  	initFactory(filterConfig);
  	factory.getLocators();
    Assert.assertTrue(factory.getLocators().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidUriLocatorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    factory.getLocators();
  }

  @Test
  public void testWhenValidLocatorsSet() {
    configureValidUriLocators(filterConfig);
    Assert.assertEquals(3, factory.getLocators().size());
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
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("bomStripper, cssImport, cssVariables");
    initFactory(filterConfig);
    final List<ResourcePreProcessor> list = factory.getPreProcessors();
//    Collections.sort(list, new Comparator<ResourcePreProcessor>() {
//      public int compare(final ResourcePreProcessor o1, final ResourcePreProcessor o2) {
//        return o1.hashCode() - o2.hashCode();
//      }
//    });
    Assert.assertEquals(BomStripperPreProcessor.class, list.get(0).getClass());
    Assert.assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    Assert.assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }

  @Test
  public void testWithEmptyPreProcessors() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    Assert.assertTrue(factory.getPreProcessors().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    factory.getPreProcessors();
  }

  @Test
  public void testWhenValidPreProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("cssUrlRewriting");
    initFactory(filterConfig);
    Assert.assertEquals(1, factory.getPreProcessors().size());
  }

  @Test
  public void testWithEmptyPostProcessors() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    Assert.assertTrue(factory.getPostProcessors().isEmpty());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    factory.getPostProcessors();
  }

  @Test
  public void testWhenValidPostProcessorsSet() {
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("cssMinJawr, jsMin, cssVariables");
    initFactory(filterConfig);
    Assert.assertEquals(3, factory.getPostProcessors().size());
  }
}
