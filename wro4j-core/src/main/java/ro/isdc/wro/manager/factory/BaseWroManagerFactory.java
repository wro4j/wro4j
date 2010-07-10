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
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;

/**
 * A simple implementation of {@link WroManagerFactory} which doesn't define any processors or uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Dec 30, 2009
 */
public abstract class BaseWroManagerFactory implements WroManagerFactory, WroConfigurationChangeListener, CacheChangeCallbackAware {
  /**
   * Manager instance. Using volatile keyword fix the problem with
   * double-checked locking in JDK 1.5.
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
    onBeforeCreate();
    // use double-check locking
    if (this.manager == null) {
      synchronized (this) {
        if (this.manager == null) {
          final GroupExtractor groupExtractor = newGroupExtractor();
          //TODO pass servletContext to this method - it could be useful to access it when creating model.
          final WroModelFactory modelFactory = newModelFactory(Context.get().getServletContext());
          final GroupsProcessor groupsProcessor = newGroupsProcessor();
          final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy = newCacheStrategy();
          // it is important to instantiate dependencies first, otherwise another thread can start working with
          // uninitialized manager.
          this.manager = newManager();
          manager.setGroupExtractor(groupExtractor);
          manager.setModelFactory(modelFactory);
          manager.setGroupsProcessor(groupsProcessor);
          manager.setCacheStrategy(cacheStrategy);
          manager.registerCallback(cacheChangeCallback);
        }
      }
    }
    return this.manager;
  }

  /**
   * {@inheritDoc}
   */
  public void registerCallback(final PropertyChangeListener callback) {
    this.cacheChangeCallback = callback;
  }

  /**
   * Life-cycle method. Allow subclasses to initialize context before the manager is instantiated.<br>
   * Usually, you will do here something like this:
   * <code>
   * Context myContext = ...
   * Context.set(myContext);
   * </code>
   * in order to be able to set a context associated with current request cycle.
   */
  protected void onBeforeCreate() {}

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

  /**
   * @return {@link GroupsProcessor} configured processor.
   */
  protected GroupsProcessor newGroupsProcessor() {
    return new GroupsProcessorImpl();
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
