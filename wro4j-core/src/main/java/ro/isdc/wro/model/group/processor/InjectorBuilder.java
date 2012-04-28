/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.FallbackAwareWroModelFactory;
import ro.isdc.wro.model.factory.InMemoryCacheableWroModelFactory;
import ro.isdc.wro.model.factory.InjectorAwareWroModelFactoryDecorator;
import ro.isdc.wro.model.factory.ModelTransformerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.WroModelFactoryDecorator;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.InjectorAwareUriLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.InjectorAwareProcessorsFactoryDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.util.NamingStrategy;
import ro.isdc.wro.model.resource.util.NoOpNamingStrategy;
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
  private UriLocatorFactory uriLocatorFactory = new SimpleUriLocatorFactory();
  private ProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
  private NamingStrategy namingStrategy = new NoOpNamingStrategy();
  private WroModelFactory modelFactory = null;
  private GroupExtractor groupExtractor = null;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and
   * processed.
   */
  private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
  private Injector injector;
  /**
   * Mapping of classes to be annotated and the corresponding injected object.
   */
  private Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

  public InjectorBuilder() {
  }

  public InjectorBuilder(final WroManager manager) {
    setWroManager(manager);
  }

  private void initMap() {
    map.put(PreProcessorExecutor.class, new InjectorObjectFactory<PreProcessorExecutor>() {
      public PreProcessorExecutor create() {
        return decorate(preProcessorExecutor);
      }
    });
    map.put(GroupsProcessor.class, groupsProcessor);
    map.put(LifecycleCallbackRegistry.class, callbackRegistry);
    map.put(GroupExtractor.class, groupExtractor);
    map.put(Injector.class, new InjectorObjectFactory<Injector>() {
      public Injector create() {
        return injector;
      }
    });
    map.put(UriLocatorFactory.class, new InjectorObjectFactory<UriLocatorFactory>() {
      public UriLocatorFactory create() {
        return new InjectorAwareUriLocatorFactoryDecorator(uriLocatorFactory, injector);
      }
    });
    map.put(ProcessorsFactory.class, new InjectorObjectFactory<ProcessorsFactory>() {
      public ProcessorsFactory create() {
        return new InjectorAwareProcessorsFactoryDecorator(processorsFactory, injector);
      }
    });
    map.put(WroModelFactory.class, new InjectorObjectFactory<WroModelFactory>() {
      public WroModelFactory create() {
        return modelFactory != null ? new InjectorAwareWroModelFactoryDecorator(decorate(modelFactory),
            injector) : null;
      }
    });
    map.put(NamingStrategy.class, new InjectorObjectFactory<NamingStrategy>() {
      public NamingStrategy create() {
        if (namingStrategy != null) {
          injector.inject(namingStrategy);
        }
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
        return cacheStrategy;
      }
    });
  }

  /**
   * Decorates {@link PreProcessorExecutor} with callback invocations.
   */
  private PreProcessorExecutor decorate(final PreProcessorExecutor preProcessorExecutor) {
    return new PreProcessorExecutor() {
      @Override
      public String processAndMerge(final List<Resource> resources, final boolean minimize) throws IOException {
        callbackRegistry.onBeforeMerge();
        try {
          return preProcessorExecutor.processAndMerge(resources, minimize);
        } finally {
          callbackRegistry.onAfterMerge();
        }
      }
    };
  }

  /**
   * Decorates the model factory with callback registry calls & other useful factories.
   */
  private WroModelFactory decorate(final WroModelFactory modelFactory) {
    return new ModelTransformerFactory(new InMemoryCacheableWroModelFactory(new FallbackAwareWroModelFactory(
        new WroModelFactoryDecorator(modelFactory) {
          @Override
          public WroModel create() {
            callbackRegistry.onBeforeModelCreated();
            try {
              return super.create();
        } finally {
          callbackRegistry.onAfterModelCreated();
        }
      }
    }))).setTransformers(modelTransformers);
  }

  public Injector build() {
    //first initialize the map
    initMap();
    injector = new Injector(Collections.unmodifiableMap(map));

    //process dependencies for several fields too.
    injector.inject(preProcessorExecutor);
    injector.inject(groupsProcessor);

    return injector;
  }

  public InjectorBuilder setWroManager(final WroManager manager) {
    Validate.notNull(manager);
    uriLocatorFactory = manager.getUriLocatorFactory();
    processorsFactory = manager.getProcessorsFactory();
    namingStrategy = manager.getNamingStrategy();
    modelFactory = manager.getModelFactory();
    groupExtractor = manager.getGroupExtractor();
    cacheStrategy = manager.getCacheStrategy();
    return this;
  }


  /**
   * @param namingStrategy the namingStrategy to set
   */
  public InjectorBuilder setNamingStrategy(final NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
    return this;
  }


  /**
   * @param uriLocatorFactory the uriLocatorFactory to set
   */
  public InjectorBuilder setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
    return this;
  }


  /**
   * @param processorsFactory the processorsFactory to set
   */
  public InjectorBuilder setProcessorsFactory(final ProcessorsFactory processorsFactory) {
    this.processorsFactory = processorsFactory;
    return this;
  }


  /**
   * @param preProcessorExecutor the preProcessorExecutor to set
   */
  public InjectorBuilder setPreProcessorExecutor(final PreProcessorExecutor preProcessorExecutor) {
    this.preProcessorExecutor = preProcessorExecutor;
    return this;
  }
  

  public InjectorBuilder setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
    this.modelTransformers = modelTransformers;
    return this;
  }
  
  /**
   * A special type used for lazy object injection only in context of this class.
   */
  static interface InjectorObjectFactory<T>
    extends ObjectFactory<T> {
  };
}
