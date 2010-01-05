/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;

/**
 * TestConfigurableWroManagerFactory.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory factory;

  public void initFactory(final FilterConfig filterConfig) {
    factory = new ConfigurableWroManagerFactory() {
    	@Override
    	protected void onBeforeCreate() {
    		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    		Context.set(new Context(request, response, filterConfig));
    	}
    };
    //create one instance for test
    factory.getInstance();
  }

  @After
  public void tearDown() {
  	Context.unset();
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseFactoryWithoutUriLocatorsParamSet() {
  	final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
  	initFactory(filterConfig);
  	factory.getInstance();
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseWithEmptyUriLocators() {
  	final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
  	Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("");
  	initFactory(filterConfig);
  	factory.getLocators();
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
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
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

  @Test(expected=WroRuntimeException.class)
  public void cannotUseWithEmptyPreProcessors() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    factory.getPreProcessors();
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
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_PRE_PROCESSORS)).thenReturn("cssUrlRewriting");
    initFactory(filterConfig);
    Assert.assertEquals(1, factory.getPreProcessors().size());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseWithEmptyPostProcessors() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    initFactory(filterConfig);
    factory.getPostProcessors();
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("INVALID1,INVALID2");
    initFactory(filterConfig);
    factory.getPostProcessors();
  }

  @Test
  public void testWhenValidPostProcessorsSet() {
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    configureValidUriLocators(filterConfig);
    Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_POST_PROCESSORS)).thenReturn("cssMinJawr, jsMin, cssVariables");
    initFactory(filterConfig);
    Assert.assertEquals(3, factory.getPostProcessors().size());
  }
}
