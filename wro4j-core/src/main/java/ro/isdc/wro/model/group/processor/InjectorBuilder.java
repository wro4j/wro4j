/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.util.NamingStrategy;
import ro.isdc.wro.model.resource.util.NoOpNamingStrategy;
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
  private GroupsProcessor groupsProcessor = new GroupsProcessor();
  private PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  private LifecycleCallbackRegistry callbackRegistry = new LifecycleCallbackRegistry();
  //TODO set a not null locatorFactory
  private ResourceLocatorFactory resourceLocatorFactory = null;
  private ProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
  private NamingStrategy namingStrategy = new NoOpNamingStrategy();
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
    map.put(PreProcessorExecutor.class, preProcessorExecutor);
    map.put(GroupsProcessor.class, groupsProcessor);
    map.put(LifecycleCallbackRegistry.class, callbackRegistry);
    //map.put(UriLocatorFactory.class, uriLocatorFactory);
    map.put(ResourceLocatorFactory.class, resourceLocatorFactory);
    map.put(ProcessorsFactory.class, processorsFactory);
    map.put(NamingStrategy.class, namingStrategy);
    map.put(Injector.class, new InjectorObjectFactory<Injector>() {
      public Injector create() {
        return injector;
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
    resourceLocatorFactory = manager.getResourceLocatorFactory();
    processorsFactory = manager.getProcessorsFactory();
    namingStrategy = manager.getNamingStrategy();
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
  public InjectorBuilder setResourceLocatorFactory(final ResourceLocatorFactory resourceLocatorFactory) {
    this.resourceLocatorFactory = resourceLocatorFactory;
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

  /**
   * A special type used for lazy object injection only in context of this class.
   */
  static interface InjectorObjectFactory<T>
    extends ObjectFactory<T> {
  };
}
