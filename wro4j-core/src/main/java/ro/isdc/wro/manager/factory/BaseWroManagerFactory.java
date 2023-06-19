/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.cache.factory.DefaultCacheKeyFactory;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.config.metadata.DefaultMetaDataFactory;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.DefaultProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.SHA1HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NoOpNamingStrategy;
import ro.isdc.wro.util.DestroyableLazyInitializer;
import ro.isdc.wro.util.Transformer;


/**
 * Default implementation of {@link WroManagerFactory} which creates default locators and processors and handles the
 * injection logic by creating an {@link Injector} and injecting where it is appropriate.
 *
 * @author Alex Objelean
 */
public class BaseWroManagerFactory
    implements WroManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(BaseWroManagerFactory.class);

  private GroupExtractor groupExtractor;
  private WroModelFactory modelFactory;
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  private HashStrategy hashStrategy;
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private List<Transformer<WroModel>> modelTransformers;
  private UriLocatorFactory uriLocatorFactory;
  private ProcessorsFactory processorsFactory;
  private NamingStrategy namingStrategy;
  private ResourceAuthorizationManager authorizationManager;
  private CacheKeyFactory cacheKeyFactory;
  private MetaDataFactory metaDataFactory;
  private ResourceWatcher resourceWatcher;
  /**
   * Handles the lazy synchronized creation of the manager
   */
  private final DestroyableLazyInitializer<WroManager> managerInitializer = new DestroyableLazyInitializer<WroManager>() {
    @Override
    protected WroManager initialize() {
      final WroManager.Builder managerBuilder = new WroManager.Builder();
      if (modelFactory == null) {
        modelFactory = newModelFactory();
      }
      if (groupExtractor == null) {
        groupExtractor = newGroupExtractor();
      }
      if (cacheStrategy == null) {
        cacheStrategy = newCacheStrategy();
      }
      if (hashStrategy == null) {
        hashStrategy = newHashStrategy();
      }
      if (modelTransformers == null) {
        modelTransformers = newModelTransformers();
      }
      if (processorsFactory == null) {
        processorsFactory = newProcessorsFactory();
      }
      if (uriLocatorFactory == null) {
        uriLocatorFactory = newUriLocatorFactory();
      }
      // use NoOp as default naming strategy
      if (namingStrategy == null) {
        namingStrategy = newNamingStrategy();
      }
      if (authorizationManager == null) {
        authorizationManager = newAuthorizationManager();
      }
      if (cacheKeyFactory == null) {
        cacheKeyFactory = newCacheKeyFactory();
      }
      if (metaDataFactory == null) {
        metaDataFactory = newMetaDataFactory();
      }
      if (groupExtractor != null) {
        managerBuilder.setGroupExtractor(groupExtractor);
      }
      if (cacheStrategy != null) {
        managerBuilder.setCacheStrategy(cacheStrategy);
      }
      if (hashStrategy != null) {
        managerBuilder.setHashStrategy(hashStrategy);
      }
      if (uriLocatorFactory != null) {
        managerBuilder.setLocatorFactory(uriLocatorFactory);
      }
      if (processorsFactory != null) {
        managerBuilder.setProcessorsFactory(processorsFactory);
      }
      if (namingStrategy != null) {
        managerBuilder.setNamingStrategy(namingStrategy);
      }
      if (modelTransformers != null) {
        managerBuilder.setModelTransformers(modelTransformers);
      }
      if (modelFactory != null) {
        managerBuilder.setModelFactory(modelFactory);
      }
      if (authorizationManager != null) {
        managerBuilder.setAuthorizationManager(authorizationManager);
      }
      if (cacheKeyFactory != null) {
        managerBuilder.setCacheKeyFactory(cacheKeyFactory);
      }
      if (metaDataFactory != null) {
        managerBuilder.setMetaDataFactory(metaDataFactory);
      }
      if (resourceWatcher == null) {
        resourceWatcher = new ResourceWatcher();
      }
      final WroManager manager = managerBuilder.build();

      onAfterInitializeManager(manager);
      return manager;
    }
  };

  /**
   * Creates default singleton instance of manager, by initializing manager dependencies with default values
   * (processors).
   */
  public final WroManager create() {
    return managerInitializer.get();
  }

  /**
   * @return default implementation of {@link ResourceAuthorizationManager}.
   */
  protected ResourceAuthorizationManager newAuthorizationManager() {
    return new DefaultResourceAuthorizationManager();
  }

  /**
   * Allows factory to do additional manager configuration after it was initialzed. One use-case is to configure
   * callbacks. Default implementation does nothing. Do not set anything else except callbacks in this method, otherwise
   * the initialization will not be performed properly.
   *
   * @param manager
   *          initialized instance of {@link WroManager}.
   */
  protected void onAfterInitializeManager(final WroManager manager) {
  }

  /**
   * @param namingStrategy
   *          the namingStrategy to set
   */
  public BaseWroManagerFactory setNamingStrategy(final NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
    return this;
  }

  /**
   * @return default implementation of modelTransformers.
   */
  protected List<Transformer<WroModel>> newModelTransformers() {
    return this.modelTransformers;
  }

  /**
   * Override to provide a different or modified default factory implementation.
   *
   * @return {@link ProcessorsFactory} object.
   */
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcessorsFactory();
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
   * @return {@link HashStrategy} instance.
   */
  protected HashStrategy newHashStrategy() {
    return new SHA1HashStrategy();
  }

  /**
   * @return default {@link NamingStrategy} to be used by this {@link WroManagerFactory}
   */
  protected NamingStrategy newNamingStrategy() {
    return new NoOpNamingStrategy();
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged(final long period) {
    try {
      managerInitializer.get().onCachePeriodChanged(period);
    } catch (final WroRuntimeException e) {
      LOG.warn("[FAIL] Unable to reload cache, probably because invoked outside of context");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged(final long period) {
    try {
      managerInitializer.get().onModelPeriodChanged(period);
    } catch (final WroRuntimeException e) {
      LOG.warn("[FAIL] Unable to reload model, probably because invoked outside of context");
    }
  }

  /**
   * @return {@link CacheStrategy} instance for resources' group caching.
   */
  protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
    return new LruMemoryCacheStrategy<CacheKey, CacheValue>();
  }

  /**
   * @return {@link GroupExtractor} implementation.
   */
  protected GroupExtractor newGroupExtractor() {
    return new DefaultGroupExtractor();
  }

  /**
   * @return default implementation of {@link CacheKeyFactory}.
   */
  protected CacheKeyFactory newCacheKeyFactory() {
    return new DefaultCacheKeyFactory();
  }

  /**
   * @return {@link WroModelFactory} implementation
   */
  protected WroModelFactory newModelFactory() {
    try {
      LOG.debug("Trying to use SmartWroModelFactory as default model factory");
      final Class<? extends WroModelFactory> smartFactoryClass = Class.forName(
          "ro.isdc.wro.extensions.model.factory.SmartWroModelFactory").asSubclass(WroModelFactory.class);
      return smartFactoryClass.getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      LOG.debug("SmartWroModelFactory is not available. Using default model factory.");
      LOG.debug("Reason: {}", e.getMessage());
    }
    return new XmlModelFactory();
  }

  /**
   * @return default implementation of {@link MetaDataFactory} used when no {@link MetaDataFactory} is set.
   */
  protected MetaDataFactory newMetaDataFactory() {
    return new DefaultMetaDataFactory();
  }

  /**
   * @param groupExtractor
   *          the groupExtractor to set
   */
  public BaseWroManagerFactory setGroupExtractor(final GroupExtractor groupExtractor) {
    this.groupExtractor = groupExtractor;
    return this;
  }

  /**
   * @param modelFactory
   *          the modelFactory to set
   */
  public BaseWroManagerFactory setModelFactory(final WroModelFactory modelFactory) {
    this.modelFactory = modelFactory;
    return this;
  }

  /**
   * @param hashStrategy
   *          the hash strategy to set
   * @return the factory, in order to chain calls.
   */
  public BaseWroManagerFactory setHashStrategy(final HashStrategy hashStrategy) {
    this.hashStrategy = hashStrategy;
    return this;
  }

  public void setCacheKeyFactory(final CacheKeyFactory cacheKeyFactory) {
    this.cacheKeyFactory = cacheKeyFactory;
  }

  /**
   * @param modelTransformers
   *          the modelTransformers to set
   */
  public BaseWroManagerFactory setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
    this.modelTransformers = modelTransformers;
    return this;
  }

  /**
   * Add a single model transformer.
   */
  public BaseWroManagerFactory addModelTransformer(final Transformer<WroModel> modelTransformer) {
    if (modelTransformers == null) {
      modelTransformers = new ArrayList<Transformer<WroModel>>();
    }
    this.modelTransformers.add(modelTransformer);
    return this;
  }

  /**
   * @param cacheStrategy
   *          the cacheStrategy to set
   */
  public BaseWroManagerFactory setCacheStrategy(final CacheStrategy<CacheKey, CacheValue> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
    return this;
  }

  /**
   * @param uriLocatorFactory
   *          the uriLocatorFactory to set
   */
  public BaseWroManagerFactory setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
    return this;
  }

  /**
   * @param processorsFactory
   *          the processorsFactory to set
   */
  public BaseWroManagerFactory setProcessorsFactory(final ProcessorsFactory processorsFactory) {
    this.processorsFactory = processorsFactory;
    return this;
  }

  public BaseWroManagerFactory setResourceAuthorizationManager(final ResourceAuthorizationManager authorizationManager) {
    this.authorizationManager = authorizationManager;
    return this;
  }

  public void setMetaDataFactory(final MetaDataFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }


  public BaseWroManagerFactory setResourceWatcher(final ResourceWatcher resourceWatcher) {
    this.resourceWatcher = resourceWatcher;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    managerInitializer.destroy();
  }
}
