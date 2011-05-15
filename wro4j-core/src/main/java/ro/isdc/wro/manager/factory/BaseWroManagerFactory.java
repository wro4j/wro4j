/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import java.beans.PropertyChangeListener;

import javax.servlet.ServletContext;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.manager.CacheChangeCallbackAware;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.FallbackAwareWroModelFactory;
import ro.isdc.wro.model.factory.ScheduledWroModelFactory;
import ro.isdc.wro.model.factory.ServletContextAwareXmlModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.DefaultProcesorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.util.HashBuilder;
import ro.isdc.wro.model.resource.util.MD5HashBuilder;


/**
 * A simple implementation of {@link WroManagerFactory} which uses default processors and uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Dec 30, 2009
 */
public class BaseWroManagerFactory
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
          //decorate with scheduler ability
          final WroModelFactory modelFactory = new ScheduledWroModelFactory(new FallbackAwareWroModelFactory(
            newModelFactory(Context.get().getServletContext())));
          final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy = newCacheStrategy();
          final Injector injector = new Injector(newUriLocatorFactory(), newProcessorsFactory());
          this.manager = new WroManager(injector);
          manager.setGroupExtractor(groupExtractor);
          manager.setModelFactory(modelFactory);
          manager.setCacheStrategy(cacheStrategy);
          manager.setHashBuilder(newHashBuilder());
          manager.registerCallback(cacheChangeCallback);
        }
      }
    }
    return this.manager;
  }


  /**
   * Override to provide a different or modified factory.
   *
   * @return {@link ProcessorsFactory} object.
   */
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcesorsFactory();
  }


  /**
   * Override to provide a different or modified factory.
   *
   * @return {@link UriLocatorFactory} object.
   */
  protected UriLocatorFactory newUriLocatorFactory() {
    return new DefaultUriLocatorFactory();
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
   * @return {@link CacheStrategy} instance for resources' group caching.
   */
  protected CacheStrategy<CacheEntry, ContentHashEntry> newCacheStrategy() {
    return new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>();
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
   * {@inheritDoc}
   */
  public void destroy() {
    //there is a strange situation when manager actually can be null
    if (manager != null) {
      manager.destroy();
    }
  }
}
