/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.FilterConfig;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.config.support.DeploymentMode;

/**
 * @author Alex Objelean
 */
public class TestFilterConfigWroConfigurationFactory {
  @Mock
  private FilterConfig filterConfig;
  private FilterConfigWroConfigurationFactory factory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected=NullPointerException.class)
  public void cannotUseNullArgument() {
    factory = new FilterConfigWroConfigurationFactory(null);
  }

  @Test
  public void testConfigureCacheUpdatePeriod() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.getPropertyKey())).thenReturn("10");
    factory = new FilterConfigWroConfigurationFactory(filterConfig);
    final WroConfiguration config = factory.create();
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(true, config.isDebug());
  }

  @Test
  public void testConfigureDebug() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.debug.getPropertyKey())).thenReturn("false");
    factory = new FilterConfigWroConfigurationFactory(filterConfig);
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }

  /**
   * This test ensures that when "configuration" init-param is used, it will override the debug property for backward compatibility.
   *
   */
  @Test
  public void testConfigurationTypeBackwardCompatibility() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.debug.getPropertyKey())).thenReturn("true");
    Mockito.when(filterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
    		DeploymentMode.DEPLOYMENT.toString());
    factory = new FilterConfigWroConfigurationFactory(filterConfig);
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }
}
