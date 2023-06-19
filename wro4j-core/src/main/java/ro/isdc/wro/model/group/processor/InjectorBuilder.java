/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.ResourceBundleProcessor;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.SimpleWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.locator.factory.InjectableUriLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.change.ResourceChangeDetector;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.ProxyFactory;
import ro.isdc.wro.util.ProxyFactory.TypedObjectFactory;


/**
 * Responsible for building the {@link Injector}. It can build an {@link Injector} without needing a {@link WroManager},
 * but just by providing required dependencies.
 *
 * @author Alex Objelean
 * @since 1.4.3
 */
public class InjectorBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(InjectorBuilder.class);
  private final GroupsProcessor groupsProcessor = new GroupsProcessor();
  private final PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  private final ResourceChangeDetector resourceChangeDetector = new ResourceChangeDetector();
  private final ResourceBundleProcessor bundleProcessor = new ResourceBundleProcessor();
  private ResourceWatcher resourceWatcher = new ResourceWatcher();
  private DispatcherStreamLocator dispatcherLocator = new DispatcherStreamLocator();
  private Injector injector;
  /**
   * Mapping of classes to be annotated and the corresponding injected object. TODO: probably replace this map with
   * something like spring ApplicationContext (lightweight IoC).
   */
  private final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
  private WroManagerFactory managerFactory;

  /**
   * Use factory method {@link InjectorBuilder#create(WroManagerFactory)} instead.
   */
  public InjectorBuilder() {
	  super();
	  LOG.warn("The default constructor should only be used for testing purposes.");
  }

  public InjectorBuilder(final WroManagerFactory managerFactory) {
    notNull(managerFactory);
    this.managerFactory = managerFactory;
  }

  /**
   * Factory method which uses a managerFactory to initialize injected fields.
   */
  public static InjectorBuilder create(final WroManagerFactory managerFactory) {
    return new InjectorBuilder(managerFactory);
  }

  public static InjectorBuilder create(final WroManager manager) {
    return new InjectorBuilder(new SimpleWroManagerFactory(manager));
  }

  private void initMap() {
    map.put(CacheStrategy.class, createCacheStrategyProxy());

    map.put(PreProcessorExecutor.class, createPreProcessorExecutorProxy());
    map.put(GroupsProcessor.class, createGroupsProcessorProxy());
    map.put(LifecycleCallbackRegistry.class, createCallbackRegistryProxy());
    map.put(GroupExtractor.class, createGroupExtractorProxy());
    map.put(Injector.class, createInjectorProxy());
    map.put(UriLocatorFactory.class, createLocatorFactoryProxy());
    map.put(ProcessorsFactory.class, createProcessorFactoryProxy());
    map.put(WroModelFactory.class, createModelFactoryProxy());
    map.put(NamingStrategy.class, createNamingStrategyProxy());
    map.put(HashStrategy.class, createHashStrategyProxy());
    map.put(ReadOnlyContext.class, createReadOnlyContextProxy());
    map.put(ResourceAuthorizationManager.class, createResourceAuthorizationManagerProxy());
    map.put(MetaDataFactory.class, createMetaDataFactoryProxy());
    map.put(ResourceBundleProcessor.class, createResourceBundleProcessorProxy());
    map.put(CacheKeyFactory.class, createCacheKeyFactoryProxy());
    map.put(ResourceChangeDetector.class, createResourceChangeDetectorProxy());
    map.put(ResourceWatcher.class, createResourceWatcherProxy());
    map.put(DispatcherStreamLocator.class, createDispatcherLocatorProxy());
  }

  private Object createDispatcherLocatorProxy() {
    return new InjectorObjectFactory<DispatcherStreamLocator>() {
      @Override
      public DispatcherStreamLocator create() {
        //Use the configured timeout.
        dispatcherLocator.setTimeout(Context.get().getConfig().getConnectionTimeout());
        return dispatcherLocator;
      }
    };
  }

  private Object createResourceBundleProcessorProxy() {
    return new InjectorObjectFactory<ResourceBundleProcessor>() {
      @Override
      public ResourceBundleProcessor create() {
        return bundleProcessor;
      }
    };
  }

  private Object createMetaDataFactoryProxy() {
    return new InjectorObjectFactory<MetaDataFactory>() {
      @Override
      public MetaDataFactory create() {
        return managerFactory.create().getMetaDataFactory();
      }
    };
  }

  private InjectorObjectFactory<PreProcessorExecutor> createPreProcessorExecutorProxy() {
    return new InjectorObjectFactory<PreProcessorExecutor>() {
      @Override
      public PreProcessorExecutor create() {
        return preProcessorExecutor;
      }
    };
  }

  private InjectorObjectFactory<GroupsProcessor> createGroupsProcessorProxy() {
    return new InjectorObjectFactory<GroupsProcessor>() {
      @Override
      public GroupsProcessor create() {
        return groupsProcessor;
      }
    };
  }

  private InjectorObjectFactory<LifecycleCallbackRegistry> createCallbackRegistryProxy() {
    return new InjectorObjectFactory<LifecycleCallbackRegistry>() {
      @Override
      public LifecycleCallbackRegistry create() {
        return managerFactory.create().getCallbackRegistry();
      }
    };
  }

  private InjectorObjectFactory<Injector> createInjectorProxy() {
    return new InjectorObjectFactory<Injector>() {
      @Override
      public Injector create() {
        isTrue(injector != null);
        return injector;
      }
    };
  }

  private Object createGroupExtractorProxy() {
    return new InjectorObjectFactory<GroupExtractor>() {
      @Override
      public GroupExtractor create() {
        return managerFactory.create().getGroupExtractor();
      }
    };
  }

  private Object createProcessorFactoryProxy() {
    return new InjectorObjectFactory<ProcessorsFactory>() {
      @Override
      public ProcessorsFactory create() {
        return managerFactory.create().getProcessorsFactory();
      }
    };
  }

  private Object createLocatorFactoryProxy() {
    return new InjectorObjectFactory<UriLocatorFactory>() {
      @Override
      public UriLocatorFactory create() {
        return new InjectableUriLocatorFactoryDecorator(managerFactory.create().getUriLocatorFactory());
      }
    };
  }

  private Object createResourceAuthorizationManagerProxy() {
    return new InjectorObjectFactory<ResourceAuthorizationManager>() {
      @Override
      public ResourceAuthorizationManager create() {
        return managerFactory.create().getResourceAuthorizationManager();
      }
    };
  }

  private Object createModelFactoryProxy() {
    return new InjectorObjectFactory<WroModelFactory>() {
      @Override
      public WroModelFactory create() {
        return managerFactory.create().getModelFactory();
      }
    };
  }

  private Object createNamingStrategyProxy() {
    return new InjectorObjectFactory<NamingStrategy>() {
      @Override
      public NamingStrategy create() {
        return managerFactory.create().getNamingStrategy();
      }
    };
  }

  private Object createHashStrategyProxy() {
    return new InjectorObjectFactory<HashStrategy>() {
      @Override
      public HashStrategy create() {
        return managerFactory.create().getHashStrategy();
      }
    };
  }

  @SuppressWarnings("rawtypes")
  private Object createCacheStrategyProxy() {
    return new InjectorObjectFactory<CacheStrategy>() {
      @Override
      public CacheStrategy create() {
        return managerFactory.create().getCacheStrategy();
      }
    };
  }

  /**
   * @return a proxy of {@link ReadOnlyContext} object. This solution is preferred to {@link InjectorObjectFactory}
   *         because the injected field ensure thread-safe behavior.
   */
  private Object createReadOnlyContextProxy() {
    return ProxyFactory.proxy(new TypedObjectFactory<ReadOnlyContext>() {
      @Override
      public ReadOnlyContext create() {
        return Context.get();
      }

      @Override
      public Class<? extends ReadOnlyContext> getObjectClass() {
        return Context.class;
      }
    }, ReadOnlyContext.class);
  }

  private Object createCacheKeyFactoryProxy() {
    return new InjectorObjectFactory<CacheKeyFactory>() {
      @Override
      public CacheKeyFactory create() {
        return managerFactory.create().getCacheKeyFactory();
      }
    };
  }

  private Object createResourceChangeDetectorProxy() {
    return new InjectorObjectFactory<ResourceChangeDetector>() {
      @Override
      public ResourceChangeDetector create() {
        return resourceChangeDetector;
      }
    };
  }

  private Object createResourceWatcherProxy() {
    return new InjectorObjectFactory<ResourceWatcher>() {
      @Override
      public ResourceWatcher create() {
        return resourceWatcher;
      }
    };
  }

  public InjectorBuilder setResourceWatcher(final ResourceWatcher resourceWatcher) {
    notNull(resourceWatcher);
    this.resourceWatcher = resourceWatcher;
    return this;
  }

  public InjectorBuilder setDispatcherLocator(final DispatcherStreamLocator dispatcherLocator) {
    notNull(dispatcherLocator);
    this.dispatcherLocator = dispatcherLocator;
    return this;
  }

  public Injector build() {
    // first initialize the map
    initMap();
    return injector = new Injector(Collections.unmodifiableMap(map));
  }

  /**
   * A special type used for lazy object injection only in context of this class.
   */
  static interface InjectorObjectFactory<T>
      extends ObjectFactory<T> {
  };
}
