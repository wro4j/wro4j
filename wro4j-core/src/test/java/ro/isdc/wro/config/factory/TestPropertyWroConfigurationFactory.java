/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * @author Alex Objelean
 */
public class TestPropertyWroConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestPropertyWroConfigurationFactory.class);
  private PropertyWroConfigurationFactory factory;
  @Before
  public void setUp() {
    factory = new PropertyWroConfigurationFactory();
  }

  @Test
  public void testCreateDefaultConfig() {
    factory.setProperties(null);
    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    Assert.assertNotNull(config);
    Assert.assertEquals(0, config.getModelUpdatePeriod());
    Assert.assertEquals(0, config.getCacheUpdatePeriod());
    Assert.assertEquals(true, config.isDebug());
    Assert.assertEquals(false, config.isDisableCache());
    Assert.assertEquals(true, config.isGzipEnabled());
    Assert.assertEquals(true, config.isIgnoreMissingResources());
    Assert.assertEquals(true, config.isJmxEnabled());
  }

  @Test
  public void testConfigWithProperties() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "10");
    props.setProperty(ConfigConstants.modelUpdatePeriod.name(), "20");
    props.setProperty(ConfigConstants.disableCache.name(), "true");
    factory.setProperties(props);
    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(20, config.getModelUpdatePeriod());
    Assert.assertEquals(true, config.isDisableCache());
  }

  @Test
  public void testConfigWithInvalidProperties() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "INVALID_LONG");
    props.setProperty(ConfigConstants.disableCache.name(), "INVALID_BOOLEAN");
    factory.setProperties(props);
    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    Assert.assertEquals(0, config.getCacheUpdatePeriod());
    Assert.assertEquals(false, config.isDisableCache());
  }
}
