/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.cache.DefaultSynchronizedCacheStrategyDecorator;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.DefaultWroModelFactoryDecorator;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.locator.factory.InjectorAwareUriLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Responsible for building the {@link Injector}. It can build an {@link Injector} without needing a {@link WroManager},
 * but just by providing required dependencies.
 * 
 * @author Alex Objelean
 * @since 1.4.3
 * @created 6 Jan 2012
 */
public class InjectorBuilder {
  private final GroupsProcessor groupsProcessor = new GroupsProcessor();
  private final PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private Injector injector;
  /**
   * Mapping of classes to be annotated and the corresponding injected object. TODO: probably replace this map with
   * something like spring ApplicationContext (lightweight IoC).
   */
  private final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
  private WroManagerFactory managerFactory;
  private final LazyInitializer<UriLocatorFactory> locatorFactoryInitializer = new LazyInitializer<UriLocatorFactory>() {
    @Override
    protected UriLocatorFactory initialize() {
      final WroManager manager = managerFactory.create();
      // update manager with new decorated factory
      manager.setUriLocatorFactory(InjectorAwareUriLocatorFactoryDecorator.decorate(manager.getUriLocatorFactory(),
          injector));
      return manager.getUriLocatorFactory();
    }
  };
  private LazyInitializer<ResourceAuthorizationManager> authorizationManagerInitializer = new LazyInitializer<ResourceAuthorizationManager>() {
    @Override
    protected ResourceAuthorizationManager initialize() {
      final WroManager manager = managerFactory.create();
      return manager.getResourceAuthorizationManager();
    }
  };
  
  private final LazyInitializer<WroModelFactory> modelFactoryInitializer = new LazyInitializer<WroModelFactory>() {
    @Override
    protected WroModelFactory initialize() {
      final WroManager manager = managerFactory.create();
      // update manager with new decorated factory
      manager.setModelFactory(DefaultWroModelFactoryDecorator.decorate(manager.getModelFactory(),
          manager.getModelTransformers()));
      return manager.getModelFactory();
    }
  };
  /**
   * Ensure the strategy is decorated only once.
   */
  private final LazyInitializer<CacheStrategy<CacheEntry, ContentHashEntry>> cacheStrategyInitializer = new LazyInitializer<CacheStrategy<CacheEntry, ContentHashEntry>>() {
    @Override
    protected CacheStrategy<CacheEntry, ContentHashEntry> initialize() {
      final WroManager manager = managerFactory.create();
      // update manager with new decorated strategy
      manager.setCacheStrategy(DefaultSynchronizedCacheStrategyDecorator.decorate(manager.getCacheStrategy()));
      return manager.getCacheStrategy();
    }
  };

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
    Validate.notNull(managerFactory);
    return new InjectorBuilder(managerFactory);
  }
  
  public InjectorBuilder(final WroManagerFactory managerFactory) {
    Validate.notNull(managerFactory);
    this.managerFactory = managerFactory;
  }
  
  private void initMap() {
    map.put(PreProcessorExecutor.class, new InjectorObjectFactory<PreProcessorExecutor>() {
      public PreProcessorExecutor create() {
        injector.inject(preProcessorExecutor);
        return preProcessorExecutor;
      }
    });
    map.put(GroupsProcessor.class, new InjectorObjectFactory<GroupsProcessor>() {
      public GroupsProcessor create() {
        injector.inject(groupsProcessor);
        return groupsProcessor;
      }
    });
    map.put(LifecycleCallbackRegistry.class, new InjectorObjectFactory<LifecycleCallbackRegistry>() {
      public LifecycleCallbackRegistry create() {
        final LifecycleCallbackRegistry callbackRegistry = managerFactory.create().getCallbackRegistry();
        injector.inject(callbackRegistry);
        return callbackRegistry;
      }
    });
    map.put(GroupExtractor.class, new InjectorObjectFactory<GroupExtractor>() {
      public GroupExtractor create() {
        final GroupExtractor groupExtractor = managerFactory.create().getGroupExtractor();
        injector.inject(groupExtractor);
        return groupExtractor;
      }
    });
    map.put(Injector.class, new InjectorObjectFactory<Injector>() {
      public Injector create() {
        return injector;
      }
    });
    map.put(UriLocatorFactory.class, new InjectorObjectFactory<UriLocatorFactory>() {
      public UriLocatorFactory create() {
        return locatorFactoryInitializer.get();
      }
    });
    map.put(ProcessorsFactory.class, new InjectorObjectFactory<ProcessorsFactory>() {
      public ProcessorsFactory create() {
        return managerFactory.create().getProcessorsFactory();
      }
    });
    map.put(WroModelFactory.class, new InjectorObjectFactory<WroModelFactory>() {
      public WroModelFactory create() {
        final WroModelFactory modelFactory = modelFactoryInitializer.get();
        injector.inject(modelFactory);
        return modelFactory;
      }
    });
    map.put(NamingStrategy.class, new InjectorObjectFactory<NamingStrategy>() {
      public NamingStrategy create() {
        NamingStrategy namingStrategy = managerFactory.create().getNamingStrategy();
        injector.inject(namingStrategy);
        return namingStrategy;
      }
    });
    map.put(HashStrategy.class, new InjectorObjectFactory<HashStrategy>() {
      public HashStrategy create() {
        final HashStrategy hashStrategy = managerFactory.create().getHashStrategy();
        injector.inject(hashStrategy);
        return hashStrategy;
      }
    });
    map.put(ReadOnlyContext.class, createReadOnlyContextProxy());
    map.put(WroConfiguration.class, new InjectorObjectFactory<WroConfiguration>() {
      public WroConfiguration create() {
        return Context.get().getConfig();
      }
    });
    map.put(CacheStrategy.class, new InjectorObjectFactory<CacheStrategy<CacheEntry, ContentHashEntry>>() {
      public CacheStrategy<CacheEntry, ContentHashEntry> create() {
        final CacheStrategy<CacheEntry, ContentHashEntry> decorated = cacheStrategyInitializer.get();
        injector.inject(decorated);
        return decorated;
      }
    });
    map.put(ResourceAuthorizationManager.class, new InjectorObjectFactory<ResourceAuthorizationManager>() {
      public ResourceAuthorizationManager create() {
        return authorizationManagerInitializer.get();
      }
    });
  }
  
  /**
   * @return a proxy of {@link ReadOnlyContext} object. This solution is preferred to {@link InjectorObjectFactory}
   *         because the injected field ensure thread-safe behavior.
   */
  private ReadOnlyContext createReadOnlyContextProxy() {
    InvocationHandler handler = new InvocationHandler() {
      public Object invoke(final Object proxy, final Method method, final Object[] args)
          throws Throwable {
        return method.invoke(Context.get(), args);
      }
    };
    final ReadOnlyContext readOnlyContext = (ReadOnlyContext) Proxy.newProxyInstance(
        ReadOnlyContext.class.getClassLoader(), new Class[] {
          ReadOnlyContext.class
        }, handler);
    return readOnlyContext;
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
