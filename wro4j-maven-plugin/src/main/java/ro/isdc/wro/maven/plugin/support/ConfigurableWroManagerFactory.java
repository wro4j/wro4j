/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.maven.plugin.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;

/**
 * Default implementaiton which use a property file to read the pre & post processors to be used during processing.
 *
 * @author Alex Objelean
 * @created 2 Aug 2011
 * @since 1.4.0
 */
public class ConfigurableWroManagerFactory
    extends ConfigurableStandaloneContextAwareManagerFactory implements ExtraConfigFileAware {
  private File configProperties;
  /**
   * @return a map of preProcessors.
   */
  @Override
  protected Map<String, ResourceProcessor> createPreProcessorsMap() {
    final Map<String, ResourceProcessor> map = ProcessorsUtils.createProcessorsMap();
    ExtensionsConfigurableWroManagerFactory.pupulateMapWithExtensionsProcessors(map);
    return map;
  }

  /**
   * @return a map of postProcessors.
   */
  @Override
  protected Map<String, ResourceProcessor> createPostProcessorsMap() {
    final Map<String, ResourceProcessor> map = ProcessorsUtils.createProcessorsMap();
    ExtensionsConfigurableWroManagerFactory.pupulateMapWithExtensionsProcessors(map);
    return map;
  }
  /**
   * {@inheritDoc}
   */
  @Override
  protected Properties createProperties() {
    try {
      final Properties properties = new Properties();
      properties.load(new FileInputStream(configProperties));
      return properties;
    } catch (final IOException e) {
      throw new WroRuntimeException(
          "Exception while loading properties file from " + configProperties.getAbsolutePath(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setExtraConfigFile(final File extraProperties) {
    this.configProperties = extraProperties;
  }
}
