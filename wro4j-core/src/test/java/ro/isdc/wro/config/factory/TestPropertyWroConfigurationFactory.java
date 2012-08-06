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

import ro.isdc.wro.WroRuntimeException;
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
  public void createDefaultConfig() {
    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    Assert.assertNotNull(config);
    Assert.assertEquals(0, config.getModelUpdatePeriod());
    Assert.assertEquals(0, config.getCacheUpdatePeriod());
    Assert.assertEquals(0, config.getResourceWatcherUpdatePeriod());
    Assert.assertEquals(true, config.isDebug());
    Assert.assertEquals(false, config.isDisableCache());
    Assert.assertEquals(true, config.isGzipEnabled());
    Assert.assertEquals(true, config.isIgnoreMissingResources());
    Assert.assertEquals(true, config.isIgnoreEmptyGroup());
    Assert.assertEquals(false, config.isIgnoreFailingProcessor());
    Assert.assertEquals(true, config.isJmxEnabled());
    Assert.assertEquals(false, config.isCacheGzippedContent());
    Assert.assertEquals(false, config.isParallelPreprocessing());
    Assert.assertEquals(WroConfiguration.DEFAULT_CONNECTION_TIMEOUT, config.getConnectionTimeout());
    Assert.assertEquals(WroConfiguration.DEFAULT_ENCODING, config.getEncoding());
    Assert.assertEquals(WroConfiguration.DEFAULT_CONNECTION_TIMEOUT, config.getConnectionTimeout());
  }

  @Test
  public void invalidBooleanFallbacksToFalse() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.cacheGzippedContent.name(), "INVALID_BOOLEAN");
    
    factory = new PropertyWroConfigurationFactory(props);
    final WroConfiguration config = factory.create();

    Assert.assertEquals(false, config.isCacheGzippedContent());
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
    
    factory = new PropertyWroConfigurationFactory(props);

    final WroConfiguration config = factory.create();
    LOG.debug("config: {}", config);
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(20, config.getModelUpdatePeriod());
    Assert.assertEquals(30, config.getResourceWatcherUpdatePeriod());
    Assert.assertEquals(true, config.isDisableCache());
    Assert.assertEquals(false, config.isGzipEnabled());
    Assert.assertEquals(true, config.isCacheGzippedContent());
    Assert.assertEquals(true, config.isParallelPreprocessing());
    Assert.assertEquals(false, config.isIgnoreEmptyGroup());
    Assert.assertEquals(true, config.isIgnoreFailingProcessor());
    Assert.assertEquals(5000, config.getConnectionTimeout());
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
    //The value is not a valid integer
    props.setProperty(ConfigConstants.connectionTimeout.name(), "9999999999999999999");
    
    factory = new PropertyWroConfigurationFactory(props);
    
    factory.create();
  }
}
