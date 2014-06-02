/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.config.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * @author Alex Objelean
 */
public class TestPropertyWroConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestPropertyWroConfigurationFactory.class);
  private PropertyWroConfigurationFactory factory;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    factory = new PropertyWroConfigurationFactory();
  }

  @Test
  public void createDefaultConfig() {
    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    assertNotNull(config);
    assertEquals(0, config.getModelUpdatePeriod());
    assertEquals(0, config.getCacheUpdatePeriod());
    assertEquals(0, config.getResourceWatcherUpdatePeriod());
    assertEquals(false, config.isResourceWatcherAsync());
    assertEquals(true, config.isDebug());
    assertEquals(true, config.isGzipEnabled());
    assertEquals(true, config.isIgnoreMissingResources());
    assertEquals(true, config.isIgnoreEmptyGroup());
    assertEquals(false, config.isIgnoreFailingProcessor());
    assertEquals(true, config.isJmxEnabled());
    assertEquals(false, config.isCacheGzippedContent());
    assertEquals(false, config.isParallelPreprocessing());
    assertEquals(true, config.isMinimizeEnabled());
    assertEquals(WroConfiguration.DEFAULT_CONNECTION_TIMEOUT, config.getConnectionTimeout());
    assertEquals(WroConfiguration.DEFAULT_ENCODING, config.getEncoding());
    assertEquals(WroConfiguration.DEFAULT_CONNECTION_TIMEOUT, config.getConnectionTimeout());
  }

  @Test
  public void invalidBooleanFallbacksToFalse() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheGzippedContent.name(), "INVALID_BOOLEAN");

    factory = new PropertyWroConfigurationFactory(props);
    final WroConfiguration config = factory.create();

    assertEquals(false, config.isCacheGzippedContent());
  }

  @Test
  public void configWithProperties() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "10");
    props.setProperty(ConfigConstants.modelUpdatePeriod.name(), "20");
    props.setProperty(ConfigConstants.resourceWatcherUpdatePeriod.name(), "30");
    props.setProperty(ConfigConstants.disableCache.name(), "true");
    props.setProperty(ConfigConstants.gzipResources.name(), "false");
    props.setProperty(ConfigConstants.cacheGzippedContent.name(), "true");
    props.setProperty(ConfigConstants.parallelPreprocessing.name(), "true");
    props.setProperty(ConfigConstants.ignoreEmptyGroup.name(), "false");
    props.setProperty(ConfigConstants.ignoreFailingProcessor.name(), "true");
    props.setProperty(ConfigConstants.connectionTimeout.name(), "5000");
    props.setProperty(ConfigConstants.minimizeEnabled.name(), "false");

    factory = new PropertyWroConfigurationFactory(props);

    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    assertEquals(10, config.getCacheUpdatePeriod());
    assertEquals(20, config.getModelUpdatePeriod());
    assertEquals(30, config.getResourceWatcherUpdatePeriod());
    assertEquals(false, config.isGzipEnabled());
    assertEquals(true, config.isCacheGzippedContent());
    assertEquals(true, config.isParallelPreprocessing());
    assertEquals(false, config.isIgnoreEmptyGroup());
    assertEquals(true, config.isIgnoreFailingProcessor());
    assertEquals(5000, config.getConnectionTimeout());
    assertEquals(false, config.isMinimizeEnabled());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidLong() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "INVALID_LONG");

    factory = new PropertyWroConfigurationFactory(props);

    factory.create();
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotSetInvalidConnectionTimeout() {
    final Properties props = new Properties();
    // The value is not a valid integer
    props.setProperty(ConfigConstants.connectionTimeout.name(), "9999999999999999999");

    factory = new PropertyWroConfigurationFactory(props);

    factory.create();
  }
}
