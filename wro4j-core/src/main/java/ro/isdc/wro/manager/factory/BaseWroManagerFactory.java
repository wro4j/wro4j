/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import java.beans.PropertyChangeListener;

import javax.servlet.ServletContext;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.cache.impl.MapCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.manager.CacheChangeCallbackAware;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.ServletContextAwareXmlModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.util.HashBuilder;
import ro.isdc.wro.model.resource.util.MD5HashBuilder;


/**
 * A simple implementation of {@link WroManagerFactory} which doesn't define any processors or uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Dec 30, 2009
 */
public abstract class BaseWroManagerFactory
  implements WroManagerFactory, WroConfigurationChangeListener, CacheChangeCallbackAware {
  /**
   * Manager instance. Using volatile keyword fix the problem with double-checked locking in JDK 1.5.
   */
  protected volatile WroManager manager;
  /**
   * A callback to be notified about the cache change.
   */
  private PropertyChangeListener cacheChangeCallback;
  /**
   * Prevent instantiation. Use factory method.
   */
  protected BaseWroManagerFactory() {
  }

  /**
   * Creates default singleton instance of manager, by initializing manager
   * dependencies with default values (processors).
   */
  public final WroManager getInstance() {
    // use double-check locking
    if (this.manager == null) {
      synchronized (this) {
        if (this.manager == null) {
          final GroupExtractor groupExtractor = newGroupExtractor();
          //TODO pass servletContext to this method - it could be useful to access it when creating model.
          final WroModelFactory modelFactory = newModelFactory(Context.get().getServletContext());
          final GroupsProcessor groupsProcessor = new GroupsProcessor()  {
            @Override
            protected void configureUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
              BaseWroManagerFactory.this.configureUriLocatorFactory(uriLocatorFactory);
            }
          };
          configureGroupsProcessor(groupsProcessor);
          groupsProcessor.setProcessorsFactory(newProcessorsFactory());

//          configureGroupsProcessor(groupsProcessor);
          final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy = newCacheStrategy();
          // it is important to instantiate dependencies first, otherwise another thread can start working with
          // uninitialized manager.
          this.manager = newManager();
          manager.setGroupExtractor(groupExtractor);
          manager.setModelFactory(modelFactory);
          manager.setGroupsProcessor(groupsProcessor);
          manager.setCacheStrategy(cacheStrategy);
          manager.setHashBuilder(newHashBuilder());
          manager.registerCallback(cacheChangeCallback);
        }
      }
    }
    return this.manager;
  }

  /**
   * TODO get rid of this method... it is used only to set ignoreMissingResources
   * @return {@link GroupsProcessor} configured processor.
   */
  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
  }

  /**
   * Override this method if you want to add new uri locators.
   *
   * @param factory {@link UriLocatorFactory} to configure.
   */
  protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
  }

  /**
   * @return {@link HashBuilder} instance.
   */
  protected HashBuilder newHashBuilder() {
    return new MD5HashBuilder();
  }

  /**
   * {@inheritDoc}
   */
  public void registerCallback(final PropertyChangeListener callback) {
    this.cacheChangeCallback = callback;
  }

  /**
   * @return {@link WroManager}
   */
  protected WroManager newManager() {
    return new WroManager();
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged() {
    if (manager != null) {
      manager.onCachePeriodChanged();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged() {
    if (manager != null) {
      manager.onModelPeriodChanged();
      //update cache too.
      manager.onCachePeriodChanged();
    }
  }

  /**
   * @return {@link CacheStrategy} instance.
   */
  protected CacheStrategy<CacheEntry, ContentHashEntry> newCacheStrategy() {
    return new MapCacheStrategy<CacheEntry, ContentHashEntry>();
  }

  /**
   * @return {@link GroupExtractor} implementation.
   */
  protected GroupExtractor newGroupExtractor() {
    return new DefaultGroupExtractor();
  }


  /**
   * @param servletContext {@link ServletContext} which could be useful for creating dynamic {@link WroModel}.
   * @return {@link WroModelFactory} implementation
   */
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new ServletContextAwareXmlModelFactory();
  }

//  /**
//   * @return {@link GroupsProcessor} configured processor.
//   */
//  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
//  }

  protected ProcessorsFactory newProcessorsFactory() {
    return new SimpleProcessorsFactory();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    //there is a strange situation when manager actually can be null
    if (manager != null) {
      manager.destroy();
    }
  }
}
