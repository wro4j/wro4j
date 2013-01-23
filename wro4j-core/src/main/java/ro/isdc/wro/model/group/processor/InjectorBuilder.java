/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.ResourceBundleProcessor;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.SimpleWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.ProxyFactory;


/**
 * Responsible for building the {@link Injector}. It can build an {@link Injector} without needing a {@link WroManager},
 * but just by providing required dependencies.
 *
 * @author Alex Objelean
 * @since 1.4.3
 * @created 6 Jan 2012
 */
public class InjectorBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(InjectorBuilder.class);
  private final GroupsProcessor groupsProcessor = new GroupsProcessor();
  private final PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  private ResourceBundleProcessor bundleProcessor;
  private Injector injector;
  /**
   * Mapping of classes to be annotated and the corresponding injected object. TODO: probably replace this map with
   * something like spring ApplicationContext (lightweight IoC).
   */
  private final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
  private WroManagerFactory managerFactory;

  /**
   * Use factory method {@link InjectorBuilder#create(WroManagerFactory)} instead.
   *
   * @VisibleForTesting
   */
  public InjectorBuilder() {
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

  public InjectorBuilder(final WroManagerFactory managerFactory) {
    Validate.notNull(managerFactory);
    this.managerFactory = managerFactory;
  }

  private void initMap() {
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
    map.put(WroConfiguration.class, createConfigProxy());
    map.put(CacheStrategy.class, createCacheStrategyProxy());
    map.put(ResourceAuthorizationManager.class, createResourceAuthorizationManagerProxy());
    map.put(MetaDataFactory.class, createMetaDataFactoryProxy());
    map.put(ResourceBundleProcessor.class, createResourceBundleProcessorProxy());
    map.put(CacheKeyFactory.class, createCacheKeyFactoryProxy());
  }

  private Object createResourceBundleProcessorProxy() {
    return new InjectorObjectFactory<ResourceBundleProcessor>() {
      public ResourceBundleProcessor create() {
        if (bundleProcessor == null) {
          bundleProcessor = new ResourceBundleProcessor();
        }
        return bundleProcessor;
      }
    };
  }

  private Object createMetaDataFactoryProxy() {
    return new InjectorObjectFactory<MetaDataFactory>() {
      public MetaDataFactory create() {
        return managerFactory.create().getMetaDataFactory();
      }
    };
  }

  private InjectorObjectFactory<WroConfiguration> createConfigProxy() {
    return new InjectorObjectFactory<WroConfiguration>() {
      public WroConfiguration create() {
        LOG.warn("Do not @Inject WroConfiguration. Prefer using @Inject ReadOnlyContext context; (and context.getConfig()).");
        return Context.get().getConfig();
      }
    };
  }

  private InjectorObjectFactory<PreProcessorExecutor> createPreProcessorExecutorProxy() {
    return new InjectorObjectFactory<PreProcessorExecutor>() {
      public PreProcessorExecutor create() {
        return preProcessorExecutor;
      }
    };
  }

  private InjectorObjectFactory<GroupsProcessor> createGroupsProcessorProxy() {
    return new InjectorObjectFactory<GroupsProcessor>() {
      public GroupsProcessor create() {
        return groupsProcessor;
      }
    };
  }

  private InjectorObjectFactory<LifecycleCallbackRegistry> createCallbackRegistryProxy() {
    return new InjectorObjectFactory<LifecycleCallbackRegistry>() {
      public LifecycleCallbackRegistry create() {
        return managerFactory.create().getCallbackRegistry();
      }
    };
  }

  private InjectorObjectFactory<Injector> createInjectorProxy() {
    return new InjectorObjectFactory<Injector>() {
      public Injector create() {
        return injector;
      }
    };
  }

  private Object createGroupExtractorProxy() {
    return new InjectorObjectFactory<GroupExtractor>() {
      public GroupExtractor create() {
        return managerFactory.create().getGroupExtractor();
      }
    };
  }

  private Object createProcessorFactoryProxy() {
    return new InjectorObjectFactory<ProcessorsFactory>() {
      public ProcessorsFactory create() {
        return managerFactory.create().getProcessorsFactory();
      }
    };
  }

  private Object createLocatorFactoryProxy() {
    return new InjectorObjectFactory<UriLocatorFactory>() {
      public UriLocatorFactory create() {
        return managerFactory.create().getUriLocatorFactory();
      }
    };
  }

  private Object createResourceAuthorizationManagerProxy() {
    return new InjectorObjectFactory<ResourceAuthorizationManager>() {
      public ResourceAuthorizationManager create() {
        return managerFactory.create().getResourceAuthorizationManager();
      }
    };
  }

  private Object createModelFactoryProxy() {
    return new InjectorObjectFactory<WroModelFactory>() {
      public WroModelFactory create() {
        return managerFactory.create().getModelFactory();
      }
    };
  }

  private Object createNamingStrategyProxy() {
    return new InjectorObjectFactory<NamingStrategy>() {
      public NamingStrategy create() {
        return managerFactory.create().getNamingStrategy();
      }
    };
  }

  private Object createHashStrategyProxy() {
    return new InjectorObjectFactory<HashStrategy>() {
      public HashStrategy create() {
        return managerFactory.create().getHashStrategy();
      }
    };
  }

  @SuppressWarnings("rawtypes")
  private Object createCacheStrategyProxy() {
    return new InjectorObjectFactory<CacheStrategy>() {
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
    return ProxyFactory.proxy(new ObjectFactory<ReadOnlyContext>() {
      public ReadOnlyContext create() {
        return Context.get();
      }
    }, ReadOnlyContext.class);
  }

  private Object createCacheKeyFactoryProxy() {
    return new InjectorObjectFactory<CacheKeyFactory>() {
      public CacheKeyFactory create() {
        return managerFactory.create().getCacheKeyFactory();
      }
    };
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
