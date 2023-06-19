/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.config.support.DeploymentMode;


/**
 * @author Alex Objelean
 */
public class TestPropertiesAndFilterConfigWroConfigurationFactory {
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private ServletContext mockServletContext;
  private PropertiesAndFilterConfigWroConfigurationFactory factory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(filterConfig.getServletContext()).thenReturn(mockServletContext);
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        return null;
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void cannotUseNullArgument() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(null) {
      @Override
      protected Properties newDefaultProperties(){
        return null;
      }
    };
  }

  @Test
  public void testConfigureCacheUpdatePeriod() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.getPropertyKey())).thenReturn("10");
    final WroConfiguration config = factory.create();
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(true, config.isDebug());
  }

  @Test
  public void testConfigureCacheUpdatePeriodWithPropertiesFileSet() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        final Properties props = new Properties();
        props.put(ConfigConstants.cacheUpdatePeriod.getPropertyKey(), "15");
        props.put(ConfigConstants.modelUpdatePeriod.getPropertyKey(), "30");
        return props;
      }
    };
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.getPropertyKey())).thenReturn("10");
    final WroConfiguration config = factory.create();
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(true, config.isDebug());
    // This value should be overriden
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    // This value should be the same as defined in properties
    Assert.assertEquals(30, config.getModelUpdatePeriod());
  }

  @Test
  public void testConfigureDebug() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.debug.getPropertyKey())).thenReturn("false");
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }

  @Test
  public void testConfigureDebugWithPropertiesFileSet() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        final Properties props = new Properties();
        props.put(ConfigConstants.debug.getPropertyKey(), Boolean.TRUE.toString());
        return props;
      }
    };
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.debug.getPropertyKey())).thenReturn(Boolean.FALSE.toString());
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }

  @Test
  public void testConfigureDebugWithOnlyPropertiesFileSet() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        final Properties props = new Properties();
        props.put(ConfigConstants.debug.getPropertyKey(), Boolean.TRUE.toString());
        return props;
      }
    };
    final WroConfiguration config = factory.create();
    Assert.assertEquals(true, config.isDebug());
  }

  /**
   * This test ensures that when "configuration" init-param is used, it will override the debug property for backward
   * compatibility.
   */
  @Test
  public void testConfigurationTypeBackwardCompatibility() {
    Mockito.when(filterConfig.getInitParameter(ConfigConstants.debug.getPropertyKey())).thenReturn("true");
    Mockito.when(filterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
    		DeploymentMode.DEPLOYMENT.toString());
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }

  @Test
  public void testConfigurationTypeBackwardCompatibilityWithPropertiesFileSet() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        final Properties props = new Properties();
        props.put(ConfigConstants.debug.getPropertyKey(), Boolean.TRUE.toString());
        return props;
      }
    };
    Mockito.when(filterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
    		DeploymentMode.DEPLOYMENT.toString());
    final WroConfiguration config = factory.create();
    Assert.assertEquals(false, config.isDebug());
  }

  @Test
  public void shouldBuildConfigurationEvenWhenDefaultPropertiesFileIsNotAvailable() {
    factory = new PropertiesAndFilterConfigWroConfigurationFactory(filterConfig) {
      @Override
      protected Properties newDefaultProperties(){
        throw new WroRuntimeException("Cannot build default properties found");
      }
    };
    Assert.assertNotNull(factory.create());
  }
}
