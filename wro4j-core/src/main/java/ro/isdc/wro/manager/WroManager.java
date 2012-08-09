/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.InheritableContextRunnable;
import ro.isdc.wro.config.support.WroConfigurationChangeListener;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.runnable.ReloadCacheRunnable;
import ro.isdc.wro.manager.runnable.ReloadModelRunnable;
import ro.isdc.wro.manager.runnable.ResourceWatcherRunnable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.SchedulerHelper;
import ro.isdc.wro.util.Transformer;


/**
 * Contains all the factories used by optimizer in order to perform the logic. This object should be created through
 * {@link WroManagerFactory}, in order to ensure that all dependencies are injected properly. In other words, avoid
 * setting the fields explicitly after creating a new instance of {@link WroManager}
 * 
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public class WroManager
    implements WroConfigurationChangeListener {
  private static final Logger LOG = LoggerFactory.getLogger(WroManager.class);
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private GroupExtractor groupExtractor;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  /**
   * Rename the file name based on its original name and content.
   */
  @Inject
  private NamingStrategy namingStrategy;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private WroConfiguration config;
  /**
   * HashBuilder for creating a hash based on the processed content.
   */
  @Inject
  private HashStrategy hashStrategy;
  @Inject
  private Injector injector;
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
  /**
   * Schedules the model update.
   */
  private final SchedulerHelper modelSchedulerHelper;
  /**
   * Schedules the cache update.
   */
  private final SchedulerHelper cacheSchedulerHelper;
  private final SchedulerHelper resourceWatcherSchedulerHelper;
  private ResourceBundleProcessor resourceBundleProcessor;
  
  public WroManager() {
    cacheSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new ReloadCacheRunnable(WroManager.this);
      }
    }, ReloadCacheRunnable.class.getSimpleName());
    modelSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new ReloadModelRunnable(WroManager.this);
      }
    }, ReloadModelRunnable.class.getSimpleName());
    resourceWatcherSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new InheritableContextRunnable(new ResourceWatcherRunnable(injector));
      }
    }, ReloadModelRunnable.class.getSimpleName());
    resourceBundleProcessor = new ResourceBundleProcessor();
  }
  
  /**
   * Perform processing of the uri.
   * 
   * @throws IOException
   *           when any IO related problem occurs or if the request cannot be processed.
   */
  public final void process()
      throws IOException {
    validate();
    // reschedule cache & model updates
    cacheSchedulerHelper.scheduleWithPeriod(config.getCacheUpdatePeriod());
    modelSchedulerHelper.scheduleWithPeriod(config.getModelUpdatePeriod());
    resourceWatcherSchedulerHelper.scheduleWithPeriod(config.getResourceWatcherUpdatePeriod());
    // Inject
    injector.inject(getResourceBundleProcessor());
    getResourceBundleProcessor().serveProcessedBundle();
  }

  private ResourceBundleProcessor getResourceBundleProcessor() {
    if (resourceBundleProcessor == null) {
      resourceBundleProcessor = new ResourceBundleProcessor();
    }
    return resourceBundleProcessor;
  }
  
  /**
   * Encodes a fingerprint of the resource into the path. The result may look like this: ${fingerprint}/myGroup.js
   * 
   * @return a path to the resource with the fingerprint encoded as a folder name.
   */
  public final String encodeVersionIntoGroupPath(final String groupName, final ResourceType resourceType,
      final boolean minimize) {
    final CacheEntry key = new CacheEntry(groupName, resourceType, minimize);
    final ContentHashEntry cacheValue = cacheStrategy.get(key);
    final String groupUrl = groupExtractor.encodeGroupUrl(groupName, resourceType, minimize);
    // encode the fingerprint of the resource into the resource path
    return formatVersionedResource(cacheValue.getHash(), groupUrl);
  }
  
  /**
   * Format the version of the resource in the path. Default implementation use hash as a folder: <hash>/groupName.js.
   * The implementation can be changed to follow a different versioning style, like version parameter:
   * /groupName.js?version=<hash>
   * 
   * @param hash
   *          Hash of the resource.
   * @param resourcePath
   *          Path of the resource.
   * @return formatted versioned path of the resource.
   */
  protected String formatVersionedResource(final String hash, final String resourcePath) {
    return String.format("%s/%s", hash, resourcePath);
  }

  /**
   * {@inheritDoc}
   */
  public final void onCachePeriodChanged(final long period) {
    LOG.info("onCachePeriodChanged with value {} has been triggered!", period);
    cacheSchedulerHelper.scheduleWithPeriod(period);
    // flush the cache by destroying it.
    cacheStrategy.clear();
  }
  
  /**
   * {@inheritDoc}
   */
  public final void onModelPeriodChanged(final long period) {
    LOG.info("onModelPeriodChanged with value {} has been triggered!", period);
    // trigger model destroy
    getModelFactory().destroy();
    modelSchedulerHelper.scheduleWithPeriod(period);
  }
  
  /**
   * Called when {@link WroManager} is being taken out of service.
   */
  public final void destroy() {
    try {
      cacheSchedulerHelper.destroy();
      modelSchedulerHelper.destroy();
      resourceWatcherSchedulerHelper.destroy();
      cacheStrategy.destroy();
      modelFactory.destroy();
    } catch (final Exception e) {
      LOG.error("Exception occured during manager destroy!!!");
    } finally {
      LOG.debug("WroManager destroyed");
    }
  }
  
  /**
   * Check if all dependencies are set.
   */
  private void validate() {
    Validate.notNull(cacheStrategy, "cacheStrategy was not set!");
    Validate.notNull(groupsProcessor, "groupsProcessor was not set!");
    Validate.notNull(uriLocatorFactory, "uriLocatorFactory was not set!");
    Validate.notNull(processorsFactory, "processorsFactory was not set!");
    Validate.notNull(groupExtractor, "GroupExtractor was not set!");
    Validate.notNull(modelFactory, "ModelFactory was not set!");
    Validate.notNull(cacheStrategy, "cacheStrategy was not set!");
    Validate.notNull(hashStrategy, "HashBuilder was not set!");
  }
  
  /**
   * @param groupExtractor
   *          the uriProcessor to set
   */
  public final WroManager setGroupExtractor(final GroupExtractor groupExtractor) {
    Validate.notNull(groupExtractor);
    this.groupExtractor = groupExtractor;
    return this;
  }
  
  public final WroManager setModelFactory(final WroModelFactory modelFactory) {
    Validate.notNull(modelFactory);
    this.modelFactory = modelFactory;
    return this;
  }
  
  /**
   * @param cacheStrategy
   *          the cache to set
   */
  public final WroManager setCacheStrategy(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    Validate.notNull(cacheStrategy);
    this.cacheStrategy = cacheStrategy;
    return this;
  }
  
  /**
   * @param hashStrategy
   *          the contentDigester to set
   */
  public final WroManager setHashStrategy(final HashStrategy hashStrategy) {
    Validate.notNull(hashStrategy);
    this.hashStrategy = hashStrategy;
    return this;
  }
  
  public final HashStrategy getHashStrategy() {
    return hashStrategy;
  }
  
  /**
   * @return the modelFactory
   */
  public final WroModelFactory getModelFactory() {
    return modelFactory;
  }
  
  /**
   * @return the processorsFactory used by this WroManager.
   */
  public final ProcessorsFactory getProcessorsFactory() {
    return processorsFactory;
  }
  
  /**
   * @param processorsFactory
   *          the processorsFactory to set
   */
  public final WroManager setProcessorsFactory(final ProcessorsFactory processorsFactory) {
    this.processorsFactory = processorsFactory;
    return this;
  }
  
  public final void setNamingStrategy(final NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }
  
  /**
   * @param uriLocatorFactory
   *          the uriLocatorFactory to set
   */
  public final WroManager setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
    return this;
  }
  
  /**
   * @return the cacheStrategy
   */
  public final CacheStrategy<CacheEntry, ContentHashEntry> getCacheStrategy() {
    return cacheStrategy;
  }
  
  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return uriLocatorFactory;
  }
  
  /**
   * @return The strategy used to rename bundled resources.
   */
  public final NamingStrategy getNamingStrategy() {
    return this.namingStrategy;
  }
  
  public final GroupExtractor getGroupExtractor() {
    return groupExtractor;
  }
  
  public final GroupsProcessor getGroupsProcessor() {
    return this.groupsProcessor;
  }
  
  /**
   * Registers a callback.
   * 
   * @param callback
   *          {@link LifecycleCallback} to register.
   */
  public final void registerCallback(final LifecycleCallback callback) {
    Validate.notNull(callback);
    getCallbackRegistry().registerCallback(callback);
  }
  
  public final List<Transformer<WroModel>> getModelTransformers() {
    return modelTransformers;
  }
  
  public final void setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
    this.modelTransformers = modelTransformers;
  }
 
  
  public LifecycleCallbackRegistry getCallbackRegistry() {
    // TODO check if initialization is required.
    if (callbackRegistry == null) {
      callbackRegistry = new LifecycleCallbackRegistry();
    }
    return callbackRegistry;
  }
}