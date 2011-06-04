/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;

/**
 * @author Alex Objelean
 * @created Created on Jul 25, 2010
 */
public class TestConfigurableWroFilter {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFilterWithPropertiesSet() throws Exception {
    final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        Assert.assertEquals(10, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    final Properties properties = new Properties();
    properties.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "10");
    filter.setProperties(properties);
    filter.init(Mockito.mock(FilterConfig.class));
    filter.doFilter(request, response, filterChain);
  }

  @Test
  public void testFilterWithCacheUpdatePeriodSet() throws Exception {
    final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        Assert.assertEquals(20, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    filter.setCacheUpdatePeriod(20);
    filter.init(Mockito.mock(FilterConfig.class));
    filter.doFilter(request, response, filterChain);
  }
}
