/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.cache.DefaultSynchronizedCacheStrategyDecorator;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.DefaultWroModelFactoryDecorator;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.InjectorAwareResourceLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.util.HashBuilder;
import ro.isdc.wro.model.resource.util.NamingStrategy;
import ro.isdc.wro.model.resource.util.NoOpNamingStrategy;
import ro.isdc.wro.model.resource.util.SHA1HashBuilder;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.Transformer;


/**
 * Responsible for building the {@link Injector}. It can build an {@link Injector} without needing a {@link WroManager},
 * but just by providing required dependencies.
 *
 * @author Alex Objelean
 * @since 1.4.3
 * @created 6 Jan 2012
 */
public class InjectorBuilder {
  private GroupsProcessor groupsProcessor = new GroupsProcessor();
  private PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  private LifecycleCallbackRegistry callbackRegistry = new LifecycleCallbackRegistry();
  //TODO set a not null locatorFactory
  private ResourceLocatorFactory resourceLocatorFactory = DefaultResourceLocatorFactory.contextAwareFactory();
  private ProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
  private NamingStrategy namingStrategy = new NoOpNamingStrategy();
  private HashBuilder hashBuilder = new SHA1HashBuilder();
  private WroModelFactory modelFactory = null;
  private GroupExtractor groupExtractor = null;
  /**
   * A cacheStrategy used for caching processed results.
   */
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy = new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>();
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
  private Injector injector;
  /**
   * Mapping of classes to be annotated and the corresponding injected object.
   * TODO: probably replace this map with something like spring ApplicationContext (lightweight IoC).
   */
  private Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

  /**
   * @deprecated prefer using factory method {@link InjectorBuilder#create(WroManagerFactory)}
   */
  public InjectorBuilder() {
  }

  /**
   * Factory method which uses a managerFactory to initialize injected fields.
   */
  public static InjectorBuilder create(final WroManagerFactory managerFactory) {
    Validate.notNull(managerFactory);
    return new InjectorBuilder(managerFactory.create());
  }
  
  public InjectorBuilder(final WroManager manager) {
    setWroManager(manager);
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
        injector.inject(callbackRegistry);
        return callbackRegistry;
      }
    });
    map.put(GroupExtractor.class, new InjectorObjectFactory<GroupExtractor>() {
      public GroupExtractor create() {
        injector.inject(groupExtractor);
        return groupExtractor;
      }
    });
    map.put(Injector.class, new InjectorObjectFactory<Injector>() {
      public Injector create() {
        return injector;
      }
    });
    map.put(ResourceLocatorFactory.class, new InjectorObjectFactory<ResourceLocatorFactory>() {
      public ResourceLocatorFactory create() {
        return new InjectorAwareResourceLocatorFactoryDecorator(resourceLocatorFactory, injector);
      }
    });
    map.put(ProcessorsFactory.class, new InjectorObjectFactory<ProcessorsFactory>() {
      public ProcessorsFactory create() {
        return processorsFactory;
      }
    });
    map.put(WroModelFactory.class, new InjectorObjectFactory<WroModelFactory>() {
      public WroModelFactory create() {
        final WroModelFactory decorated = new DefaultWroModelFactoryDecorator(modelFactory, modelTransformers);
        injector.inject(decorated);
        return decorated;
      }
    });
    map.put(NamingStrategy.class, new InjectorObjectFactory<NamingStrategy>() {
      public NamingStrategy create() {
        injector.inject(namingStrategy);
        return namingStrategy;
      }
    });
    map.put(Context.class, new InjectorObjectFactory<Context>() {
      public Context create() {
        return Context.get();
      }
    });
    map.put(WroConfiguration.class, new InjectorObjectFactory<WroConfiguration>() {
      public WroConfiguration create() {
        return Context.get().getConfig();
      }
    });
    map.put(CacheStrategy.class, new InjectorObjectFactory<CacheStrategy<CacheEntry, ContentHashEntry>>() {
      public CacheStrategy<CacheEntry, ContentHashEntry> create() {
        final CacheStrategy<CacheEntry, ContentHashEntry> decorated = new DefaultSynchronizedCacheStrategyDecorator(cacheStrategy);
        injector.inject(decorated);
        return decorated;
      }
    });
    map.put(HashBuilder.class, new InjectorObjectFactory<HashBuilder>() {
      public HashBuilder create() {
        return hashBuilder;
      }
    });
  }

  public Injector build() {
    //first initialize the map
    initMap();
    return injector = new Injector(Collections.unmodifiableMap(map));
  }

  private InjectorBuilder setWroManager(final WroManager manager) {
    Validate.notNull(manager);
    resourceLocatorFactory = manager.getResourceLocatorFactory();
    processorsFactory = manager.getProcessorsFactory();
    namingStrategy = manager.getNamingStrategy();
    modelFactory = manager.getModelFactory();
    groupExtractor = manager.getGroupExtractor();
    cacheStrategy = manager.getCacheStrategy();
    hashBuilder = manager.getHashBuilder();
    modelTransformers = manager.getModelTransformers();
    return this;
  }

  /**
   * A special type used for lazy object injection only in context of this class.
   */
  static interface InjectorObjectFactory<T>
    extends ObjectFactory<T> {
  };
}
