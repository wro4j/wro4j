/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.cache.impl.MapCacheStrategy;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.UriProcessorImpl;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * This factory will create a WroManager which is able to run itself outside of
 * a webContainer.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class StandAloneWroManagerFactory implements WroManagerFactory {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory
      .getLog(StandAloneWroManagerFactory.class);

  /**
   * Manager instance.
   */
  private WroManager manager;

  /**
   * Creates default instance of factory, by initializing manager dependencies
   * with default values (processors).
   */
  public final synchronized WroManager getInstance() {
    if (this.manager == null) {
      this.manager = newManager();
    }
    return this.manager;
  }

  /**
   * @param settings
   *          {@link WroSettings} object.
   * @return {@link WroManager}
   */
  private WroManager newManager() {
    final WroManager manager = new WroManager();
    manager.setUriProcessor(new UriProcessorImpl());
    manager.setModelFactory(new XmlModelFactory());
    manager.setGroupsProcessor(new GroupsProcessorImpl());
    manager.setUriLocatorFactory(newUriLocatorFactory());
    manager.setCacheStrategy(new MapCacheStrategy<String, String>());
    return manager;
  }

  /**
   * Factory method for {@link UriLocatorFactory}. Create a factory and
   * initialize the uriLocators to be used.
   *
   * @return UriLocatorFactory implementation.
   */
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();

    final List<UriLocator> resourceLocators = new ArrayList<UriLocator>();
    // populate the list. The order is important.
    resourceLocators.add(new ServletContextUriLocator());
    resourceLocators.add(new ClasspathUriLocator());
    resourceLocators.add(new UrlUriLocator());

    factory.setUriLocators(resourceLocators);
    return factory;

  }
}
