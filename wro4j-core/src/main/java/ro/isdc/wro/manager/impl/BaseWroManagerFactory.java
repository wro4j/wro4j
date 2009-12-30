/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import ro.isdc.wro.cache.impl.MapCacheStrategy;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.model.impl.ServletContextAwareXmlModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.UriProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.UriProcessorImpl;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;

/**
 * A simple implementation of {@link WroManagerFactory} which doesn't define any processors or uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Dec 30, 2009
 */
public class BaseWroManagerFactory implements WroManagerFactory {
  /**
   * Manager instance. Using volatile keyword fix the problem with
   * double-checked locking in JDK 1.5.
   */
  protected volatile WroManager manager;

  /**
   * Creates default singleton instance of manager, by initializing manager
   * dependencies with default values (processors). {@inheritDoc}
   */
  public final WroManager getInstance() {
    onBeforeCreate();
    // use double-check locking
    if (this.manager == null) {
      synchronized (this) {
        if (this.manager == null) {
          this.manager = newManager();
        }
      }
    }
    return this.manager;
  }


  /**
   * Life-cycle method. Allow subclasses to initialize context before the manager is instantiated.
   */
  protected void onBeforeCreate() {}

  /**
   * @return {@link WroManager}
   */
  private WroManager newManager() {
    final WroManager manager = new WroManager();
    manager.setUriProcessor(newUriProcessor());
    manager.setModelFactory(newModelFactory());
    manager.setGroupsProcessor(newGroupsProcessor());
    manager.setUriLocatorFactory(newUriLocatorFactory());
    manager.setCacheStrategy(newCacheStrategy());
    return manager;
  }

  /**
   * @return
   */
  private MapCacheStrategy<String, String> newCacheStrategy() {
    return new MapCacheStrategy<String, String>();
  }

  /**
   * @return {@link UriProcessor} implementation.
   */
  protected UriProcessorImpl newUriProcessor() {
    return new UriProcessorImpl();
  }

  /**
   * @return {@link WroModelFactory} implementation
   */
  protected WroModelFactory newModelFactory() {
    return new ServletContextAwareXmlModelFactory();
  }

  /**
   * @return {@link GroupsProcessor} configured processor.
   */
  protected GroupsProcessor newGroupsProcessor() {
    return new GroupsProcessorImpl();
  }

  /**
   * Factory method for {@link UriLocatorFactory}. Create a factory and
   * initialize the uriLocators to be used.
   *
   * @return UriLocatorFactory implementation.
   */
  protected UriLocatorFactory newUriLocatorFactory() {
    return new UriLocatorFactoryImpl();
  }
}
