/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.config.support.WroConfigurationChangeListener;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.runnable.ReloadCacheRunnable;
import ro.isdc.wro.manager.runnable.ReloadModelRunnable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
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
  private WroModelFactory modelFactory;
  private GroupExtractor groupExtractor;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  private ProcessorsFactory processorsFactory;
  private UriLocatorFactory locatorFactory;
  /**
   * Rename the file name based on its original name and content.
   */
  @Inject
  private NamingStrategy namingStrategy;
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private ReadOnlyContext context;
  /**
   * HashBuilder for creating a hash based on the processed content.
   */
  private HashStrategy hashStrategy;
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
  /**
   * Schedules the model update.
   */
  private final SchedulerHelper modelSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
    @Override
    protected Runnable initialize() {
      //decorate with ContextPropagatingCallable to make context available in the new thread
      return ContextPropagatingCallable.decorate(new ReloadModelRunnable(getModelFactory()));
    }
  }, ReloadModelRunnable.class.getSimpleName());
  /**
   * Schedules the cache update.
   */
  private final SchedulerHelper cacheSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
    @Override
    protected Runnable initialize() {
      //decorate with ContextPropagatingCallable to make context available in the new thread
      return ContextPropagatingCallable.decorate(new ReloadCacheRunnable(getCacheStrategy()));
    }
  }, ReloadCacheRunnable.class.getSimpleName());
  @Inject
  private ResourceBundleProcessor resourceBundleProcessor;
  private ResourceAuthorizationManager authorizationManager;
  private CacheKeyFactory cacheKeyFactory;
  private MetaDataFactory metaDataFactory;

  private WroManager(final Builder builder) {
    this.modelFactory = builder.modelFactory;
    this.authorizationManager = builder.authorizationManager;
    this.cacheKeyFactory = builder.cacheKeyFactory;
    this.cacheStrategy = builder.cacheStrategy;
    this.callbackRegistry = builder.callbackRegistry;
    this.groupExtractor = builder.groupExtractor;
    this.groupsProcessor = builder.groupsProcessor;
    this.hashStrategy = builder.hashStrategy;
    this.locatorFactory = builder.locatorFactory;
    this.metaDataFactory = builder.metaDataFactory;
    this.modelTransformers = builder.modelTransformers;
    this.namingStrategy = builder.namingStrategy;
    this.processorsFactory = builder.processorsFactory;
  }

  @Deprecated
  public WroManager() {
  }

  /**
   * Perform processing of the uri.
   *
   * @throws IOException
   *           when any IO related problem occurs or if the request cannot be processed.
   */
  public final void process()
      throws IOException {
    // reschedule cache & model updates
    final WroConfiguration config = context.getConfig();
    cacheSchedulerHelper.scheduleWithPeriod(config.getCacheUpdatePeriod());
    modelSchedulerHelper.scheduleWithPeriod(config.getModelUpdatePeriod());
    resourceBundleProcessor.serveProcessedBundle();
  }

  /**
   * Encodes a fingerprint of the resource into the path. The result may look like this: ${fingerprint}/myGroup.js
   *
   * @return a path to the resource with the fingerprint encoded as a folder name.
   */
  public final String encodeVersionIntoGroupPath(final String groupName, final ResourceType resourceType,
      final boolean minimize) {
    //TODO use CacheKeyFactory
    final CacheKey key = new CacheKey(groupName, resourceType, minimize);
    final CacheValue cacheValue = cacheStrategy.get(key);
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
      cacheStrategy.destroy();
      modelFactory.destroy();
    } catch (final Exception e) {
      LOG.error("Exception occured during manager destroy!!!");
    } finally {
      LOG.debug("WroManager destroyed");
    }
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
   * @return the cacheStrategy
   */
  public final CacheStrategy<CacheKey, CacheValue> getCacheStrategy() {
    return cacheStrategy;
  }

  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return locatorFactory;
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


  public CacheKeyFactory getCacheKeyFactory() {
    return cacheKeyFactory;
  }

  public MetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  /**
   * Registers a callback.
   *
   * @param callback
   *          {@link LifecycleCallback} to register.
   */
  public final void registerCallback(final LifecycleCallback callback) {
    notNull(callback);
    getCallbackRegistry().registerCallback(callback);
  }

  public final List<Transformer<WroModel>> getModelTransformers() {
    return modelTransformers;
  }

  public LifecycleCallbackRegistry getCallbackRegistry() {
    // TODO check if initialization is required.
    if (callbackRegistry == null) {
      callbackRegistry = new LifecycleCallbackRegistry();
    }
    return callbackRegistry;
  }

  public ResourceAuthorizationManager getResourceAuthorizationManager() {
    return authorizationManager;
  }

  public static class Builder {
    private WroModelFactory modelFactory;
    private GroupExtractor groupExtractor;
    private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
    private ProcessorsFactory processorsFactory;
    private UriLocatorFactory locatorFactory;
    private NamingStrategy namingStrategy;
    private LifecycleCallbackRegistry callbackRegistry;
    private GroupsProcessor groupsProcessor;
    private HashStrategy hashStrategy;
    private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
    private ResourceAuthorizationManager authorizationManager;
    private CacheKeyFactory cacheKeyFactory;
    private MetaDataFactory metaDataFactory;
    public Builder setModelFactory(final WroModelFactory modelFactory) {
      this.modelFactory = modelFactory;
      return this;
    }
    public Builder setGroupExtractor(final GroupExtractor groupExtractor) {
      this.groupExtractor = groupExtractor;
      return this;
    }
    public Builder setCacheStrategy(final CacheStrategy<CacheKey, CacheValue> cacheStrategy) {
      this.cacheStrategy = cacheStrategy;
      return this;
    }
    public Builder setProcessorsFactory(final ProcessorsFactory processorsFactory) {
      this.processorsFactory = processorsFactory;
      return this;
    }
    public Builder setLocatorFactory(final UriLocatorFactory locatorFactory) {
      this.locatorFactory = locatorFactory;
      return this;
    }
    public Builder setNamingStrategy(final NamingStrategy namingStrategy) {
      this.namingStrategy = namingStrategy;
      return this;
    }
    public Builder setCallbackRegistry(final LifecycleCallbackRegistry callbackRegistry) {
      this.callbackRegistry = callbackRegistry;
      return this;
    }
    public Builder setGroupsProcessor(final GroupsProcessor groupsProcessor) {
      this.groupsProcessor = groupsProcessor;
      return this;
    }
    public Builder setHashStrategy(final HashStrategy hashStrategy) {
      this.hashStrategy = hashStrategy;
      return this;
    }
    public Builder setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
      this.modelTransformers = modelTransformers;
      return this;
    }
    public Builder setAuthorizationManager(final ResourceAuthorizationManager authorizationManager) {
      this.authorizationManager = authorizationManager;
      return this;
    }
    public Builder setCacheKeyFactory(final CacheKeyFactory cacheKeyFactory) {
      this.cacheKeyFactory = cacheKeyFactory;
      return this;
    }
    public Builder setMetaDataFactory(final MetaDataFactory metaDataFactory) {
      this.metaDataFactory = metaDataFactory;
      return this;
    }
    /**
     * Check if all dependencies are set.
     */
    private void validate() {
      notNull(cacheStrategy, "cacheStrategy was not set!");
      notNull(groupsProcessor, "groupsProcessor was not set!");
      notNull(locatorFactory, "uriLocatorFactory was not set!");
      notNull(processorsFactory, "processorsFactory was not set!");
      notNull(groupExtractor, "GroupExtractor was not set!");
      notNull(modelFactory, "ModelFactory was not set!");
      notNull(cacheStrategy, "cacheStrategy was not set!");
      notNull(hashStrategy, "HashStrategy was not set!");
      notNull(authorizationManager, "authorizationManager was not set!");
      notNull(metaDataFactory, "metaDataFactory was not set!");
      notNull(cacheKeyFactory, "CacheKeyFactory was not set!");
    }
    public WroManager build() {
      validate();
      return new WroManager(this);
    }
  }
}