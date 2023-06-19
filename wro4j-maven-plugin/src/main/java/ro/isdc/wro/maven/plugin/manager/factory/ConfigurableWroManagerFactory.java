/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.maven.plugin.manager.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.model.factory.SmartWroModelFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAware;
import ro.isdc.wro.maven.plugin.support.ExtraConfigFileAware;
import ro.isdc.wro.model.factory.ConfigurableModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.ConfigurableLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.hash.ConfigurableHashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.ConfigurableNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;


/**
 * Default implementation which use a property file to read the pre and post processors to be used during processing.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public class ConfigurableWroManagerFactory
    extends ConfigurableStandaloneContextAwareManagerFactory
    implements ExtraConfigFileAware {
  private File configProperties;

  @Override
  public void initialize(final StandaloneContext standaloneContext) {
    Context.get().setConfig(initConfiguration());
    super.initialize(standaloneContext);
  }

  private WroConfiguration initConfiguration() {
    return new PropertyWroConfigurationFactory(createProperties()).create();
  }

  @Override
  protected WroModelFactory newModelFactory() {
    return new ConfigurableModelFactory() {
      @Override
      protected Properties newProperties() {
        return createProperties();
      }

      @Override
      protected WroModelFactory getDefaultStrategy() {
        return SmartWroModelFactory.createFromStandaloneContext(getStandaloneContext());
      }
    };
  }

  @Override
  protected NamingStrategy newNamingStrategy() {
    return new ConfigurableNamingStrategy() {
      @Override
      protected Properties newProperties() {
        return createProperties();
      }
    };
  }

  @Override
  protected HashStrategy newHashStrategy() {
    return new ConfigurableHashStrategy() {
      @Override
      protected Properties newProperties() {
        return createProperties();
      }
    };
  }

  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    return new ConfigurableLocatorFactory() {
      @Override
      public UriLocator getInstance(final String uri) {
        final UriLocator locator = super.getInstance(uri);
        // ensure standalone context is provided to each locator requiring it for initialization.
        if (locator != null && locator instanceof StandaloneContextAware) {
          ((StandaloneContextAware) locator).initialize(getStandaloneContext());
        }
        return locator;
      }

      @Override
      protected Properties newProperties() {
        return createProperties();
      }
    };
  }

  @Override
  protected Properties createProperties() {
    try {
      final Properties properties = new Properties();
      properties.load(new FileInputStream(configProperties));
      return properties;
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while loading properties file from "
          + configProperties.getAbsolutePath(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setExtraConfigFile(final File extraProperties) {
    this.configProperties = extraProperties;
  }
}
