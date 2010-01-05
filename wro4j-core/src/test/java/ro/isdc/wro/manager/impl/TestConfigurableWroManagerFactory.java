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
    		System.out.println("Context SET: " + Context.get());
    	}
    };
    //create one instance for test
    factory.getInstance();
  }

  @After
  public void tearDown() {
  	Context.unset();
		System.out.println("Context Unset: ");
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseFactoryWithoutUriLocatorsSet() {
  	final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
  	initFactory(filterConfig);
  	factory.getInstance();
  }

  @Test
  public void test() {
  	final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
  	Mockito.when(filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS)).thenReturn("");
  	initFactory(filterConfig);
  	System.out.println(filterConfig);
  	System.out.println("uriLocators: " + filterConfig.getInitParameter(ConfigurableWroManagerFactory.PARAM_URI_LOCATORS));
  	System.out.println("Context IS: " + Context.get());
  	Assert.assertEquals(0, factory.getLocators());
  }
}
