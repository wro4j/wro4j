/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.cache.factory.DefaultCacheKeyFactory;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.support.DefaultSynchronizedCacheStrategyDecorator;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.metadata.DefaultMetaDataFactory;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.config.support.WroConfigurationChangeListener;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.runnable.ReloadCacheRunnable;
import ro.isdc.wro.manager.runnable.ReloadModelRunnable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.DefaultWroModelFactoryDecorator;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.SHA1HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NoOpNamingStrategy;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.SchedulerHelper;
import ro.isdc.wro.util.Transformer;


/**
 * Contains all the factories used by optimizer in order to perform the logic. This object should be created through
 * {@link WroManagerFactory}, in order to ensure that all dependencies are injected properly. In other words, avoid
 * setting the fields explicitly after creating a new instance of {@link WroManager}.
 * <p>
 * Most of the fields of this class are annotated with @Inject, in order to ensure that each instance can benefit use
 * <code>@Inject</code> annotation on its fields.
 *
 * @author Alex Objelean
 */
public class WroManager
    implements WroConfigurationChangeListener {
  private static final Logger LOG = LoggerFactory.getLogger(WroManager.class);
  @Inject
  private final WroModelFactory modelFactory;
  @Inject
  private final GroupExtractor groupExtractor;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private final CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  @Inject
  private final ProcessorsFactory processorsFactory;
  @Inject
  private final UriLocatorFactory locatorFactory;
  /**
   * Rename the file name based on its original name and content.
   */
  @Inject
  private final NamingStrategy namingStrategy;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  /**
   * HashBuilder for creating a hash based on the processed content.
   */
  @Inject
  private final HashStrategy hashStrategy;
  @Inject
  private ResourceBundleProcessor resourceBundleProcessor;
  @Inject
  private final ResourceAuthorizationManager authorizationManager;
  @Inject
  private final CacheKeyFactory cacheKeyFactory;
  @Inject
  private final MetaDataFactory metaDataFactory;
  @Inject
  private final ResourceWatcher resourceWatcher;
  /**
   * Schedules the model update.
   */
  private final SchedulerHelper modelSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
    @Override
    protected Runnable initialize() {
      // decorate with ContextPropagatingCallable to make context available in the new thread
      return ContextPropagatingCallable.decorate(new ReloadModelRunnable(getModelFactory()));
    }
  }, ReloadModelRunnable.class.getSimpleName());
  /**
   * Schedules the cache update.
   */
  private final SchedulerHelper cacheSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
    @Override
    protected Runnable initialize() {
      // decorate with ContextPropagatingCallable to make context available in the new thread
      return ContextPropagatingCallable.decorate(new ReloadCacheRunnable(getCacheStrategy()));
    }
  }, ReloadCacheRunnable.class.getSimpleName());

  private WroManager(final Builder builder) {
    this.authorizationManager = builder.authorizationManager;
    this.cacheKeyFactory = builder.cacheKeyFactory;
    this.cacheStrategy = DefaultSynchronizedCacheStrategyDecorator.decorate(builder.cacheStrategy);
    this.callbackRegistry = builder.callbackRegistry;
    this.groupExtractor = builder.groupExtractor;
    this.hashStrategy = builder.hashStrategy;
    this.locatorFactory = builder.locatorFactory;
    this.metaDataFactory = builder.metaDataFactory;
    this.namingStrategy = builder.namingStrategy;
    this.processorsFactory = builder.processorsFactory;
    this.modelFactory = DefaultWroModelFactoryDecorator.decorate(builder.modelFactory, builder.modelTransformers);
    this.resourceWatcher = new ResourceWatcher();
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
    final WroConfiguration config = Context.get().getConfig();
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
    // TODO use CacheKeyFactory
    final CacheKey key = new CacheKey(groupName, resourceType, minimize);
    final CacheValue cacheValue = cacheStrategy.get(key);
    final String groupUrl = groupExtractor.encodeGroupUrl(groupName, resourceType, minimize);
    // encode the fingerprint of the resource into the resource path
    return formatVersionedResource(cacheValue.getHash(), groupUrl);
  }

  /**
   * Format the version of the resource in the path. Default implementation use hash as a folder: {@code <hash>/groupName.js}.
   * The implementation can be changed to follow a different versioning style, like version parameter:
   * {@code /groupName.js?version=<hash>}
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

  @Override
  public final void onCachePeriodChanged(final long period) {
    LOG.info("onCachePeriodChanged with value {} has been triggered!", period);
    cacheSchedulerHelper.scheduleWithPeriod(period);
    // flush the cache by destroying it.
    cacheStrategy.clear();
  }

  @Override
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
      resourceWatcher.destroy();
      destroyProcessors();
    } catch (final Exception e) {
      LOG.error("Exception occured during manager destroy!", e);
    } finally {
      LOG.debug("WroManager destroyed");
    }
  }

  /**
   * Invokes destroy method on all {@link Destroyable} processors.
   */
  private void destroyProcessors() throws Exception {
    for (final ResourcePreProcessor processor : processorsFactory.getPreProcessors()) {
      if (processor instanceof Destroyable) {
        ((Destroyable) processor).destroy();
      }
    }
    for (final ResourcePostProcessor processor : processorsFactory.getPostProcessors()) {
      if (processor instanceof Destroyable) {
        ((Destroyable) processor).destroy();
      }
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

  public CacheKeyFactory getCacheKeyFactory() {
    return cacheKeyFactory;
  }

  public MetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  public ResourceWatcher getResourceWatcher() {
    return resourceWatcher;
  }

  /**
   * Registers a callback.
   *
   * @param callbackFactory
   *          {@link ObjectFactory} responsible for creating {@link LifecycleCallback} instance.
   */
  public final void registerCallback(final ObjectFactory<LifecycleCallback> callbackFactory) {
    notNull(callbackFactory);
    getCallbackRegistry().registerCallback(callbackFactory);
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

  private static class EmptyModelFactory
      implements WroModelFactory {
	@Override
    public WroModel create() {
      return new WroModel();
    }

	@Override
    public void destroy() {
    }
  }

  public static class Builder {
    private WroModelFactory modelFactory = new EmptyModelFactory();
    private GroupExtractor groupExtractor = new DefaultGroupExtractor();
    private CacheStrategy<CacheKey, CacheValue> cacheStrategy = new LruMemoryCacheStrategy<CacheKey, CacheValue>();
    private ProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
    private UriLocatorFactory locatorFactory = new DefaultUriLocatorFactory();
    private NamingStrategy namingStrategy = new NoOpNamingStrategy();
    private LifecycleCallbackRegistry callbackRegistry = new LifecycleCallbackRegistry();
    private HashStrategy hashStrategy = new SHA1HashStrategy();
    private List<Transformer<WroModel>> modelTransformers = createDefaultTransformers();
    private ResourceAuthorizationManager authorizationManager = new DefaultResourceAuthorizationManager();
    private CacheKeyFactory cacheKeyFactory = new DefaultCacheKeyFactory();
    private MetaDataFactory metaDataFactory = new DefaultMetaDataFactory();

    public Builder() {
    }

    public Builder(final WroManager manager) {
      notNull(manager);
      this.groupExtractor = manager.getGroupExtractor();
      this.cacheStrategy = manager.getCacheStrategy();
      this.processorsFactory = manager.getProcessorsFactory();
      this.locatorFactory = manager.getUriLocatorFactory();
      this.namingStrategy = manager.getNamingStrategy();
      this.callbackRegistry = manager.getCallbackRegistry();
      this.hashStrategy = manager.getHashStrategy();
      this.modelFactory = manager.getModelFactory();
      this.authorizationManager = manager.getResourceAuthorizationManager();
      this.cacheKeyFactory = manager.getCacheKeyFactory();
      this.metaDataFactory = manager.getMetaDataFactory();
    }

    public Builder setModelFactory(final WroModelFactory modelFactory) {
      this.modelFactory = modelFactory;
      return this;
    }

    public Builder setGroupExtractor(final GroupExtractor groupExtractor) {
      notNull(groupExtractor);
      this.groupExtractor = groupExtractor;
      return this;
    }

    public Builder setCacheStrategy(final CacheStrategy<CacheKey, CacheValue> cacheStrategy) {
      notNull(cacheStrategy);
      this.cacheStrategy = cacheStrategy;
      return this;
    }

    public Builder setProcessorsFactory(final ProcessorsFactory processorsFactory) {
      notNull(processorsFactory);
      this.processorsFactory = processorsFactory;
      return this;
    }

    public Builder setLocatorFactory(final UriLocatorFactory locatorFactory) {
      notNull(locatorFactory);
      this.locatorFactory = locatorFactory;
      return this;
    }

    public Builder setNamingStrategy(final NamingStrategy namingStrategy) {
      notNull(namingStrategy);
      this.namingStrategy = namingStrategy;
      return this;
    }

    public Builder setCallbackRegistry(final LifecycleCallbackRegistry callbackRegistry) {
      notNull(callbackRegistry);
      this.callbackRegistry = callbackRegistry;
      return this;
    }

    public Builder setHashStrategy(final HashStrategy hashStrategy) {
      notNull(hashStrategy);
      this.hashStrategy = hashStrategy;
      return this;
    }

    public Builder setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
      notNull(modelTransformers);
      this.modelTransformers = modelTransformers;
      return this;
    }

    public Builder setAuthorizationManager(final ResourceAuthorizationManager authorizationManager) {
      notNull(authorizationManager);
      this.authorizationManager = authorizationManager;
      return this;
    }

    public Builder setCacheKeyFactory(final CacheKeyFactory cacheKeyFactory) {
      notNull(cacheKeyFactory);
      this.cacheKeyFactory = cacheKeyFactory;
      return this;
    }

    public Builder setMetaDataFactory(final MetaDataFactory metaDataFactory) {
      notNull(metaDataFactory);
      this.metaDataFactory = metaDataFactory;
      return this;
    }

    private List<Transformer<WroModel>> createDefaultTransformers() {
      final List<Transformer<WroModel>> list = new ArrayList<Transformer<WroModel>>();
      list.add(new WildcardExpanderModelTransformer());
      return list;
    }

    public WroManager build() {
      final WroManager manager = new WroManager(this);
      InjectorBuilder.create(manager).build().inject(manager);
      return manager;
    }
  }
}