/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.manager.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Register all available callbacks. The registry acts as a {@link LifecycleCallback} itself whose implementation
 * delegate the call to registered callbacks. The registry will handle any runtime exceptions thrown by callbacks, in
 * order to allow successful lifecycle execution.
 *
 * @author Alex Objelean
 * @since 1.4.3
 */
public class LifecycleCallbackRegistry
  implements LifecycleCallback {
  private static final Logger LOG = LoggerFactory.getLogger(LifecycleCallbackRegistry.class);

  /**
   * The list of registered callbacks.
   */
  private final List<ObjectFactory<LifecycleCallback>> callbackFactoryList = new ArrayList<ObjectFactory<LifecycleCallback>>();
  private final Map<String, List<LifecycleCallback>> map = new ConcurrentHashMap<String, List<LifecycleCallback>>();

  /**
   * Register a callback using a factory responsible for callback instantiation.
   *
   * @param callbackFactory
   *          the factory used to instantiate callbacks.
   */
  public void registerCallback(final ObjectFactory<LifecycleCallback> callbackFactory) {
    callbackFactoryList.add(callbackFactory);
  }

  private List<LifecycleCallback> getCallbacks() {
    final String key = Context.getCorrelationId();
    List<LifecycleCallback> callbacks = map.get(key);
    if (callbacks == null) {
      callbacks = initCallbacks();
      map.put(key, callbacks);
    }
    return callbacks;
  }

  protected List<LifecycleCallback> initCallbacks() {
    final List<LifecycleCallback> callbacks = new ArrayList<LifecycleCallback>();
    for (final ObjectFactory<LifecycleCallback> callbackFactory : callbackFactoryList) {
      callbacks.add(callbackFactory.create());
    }
    return callbacks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBeforeModelCreated() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onBeforeModelCreated();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onAfterModelCreated() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onAfterModelCreated();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBeforePreProcess() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onBeforePreProcess();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onAfterPreProcess() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onAfterPreProcess();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBeforePostProcess() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onBeforePostProcess();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onAfterPostProcess() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onAfterPostProcess();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBeforeMerge() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onBeforeMerge();
        return null;
      }
    });
  }

  @Override
  public void onAfterMerge() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onAfterMerge();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onProcessingComplete() {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onProcessingComplete();
        return null;
      }
    });
  }

  @Override
  public void onResourceChanged(final Resource resource) {
    forEachCallbackDo(new Function<LifecycleCallback, Void>() {
      @Override
      public Void apply(final LifecycleCallback input)
          throws Exception {
        input.onResourceChanged(resource);
        return null;
      }
    });
  }

  private void forEachCallbackDo(final Function<LifecycleCallback, Void> func) {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        func.apply(callback);
      } catch (final Exception e) {
        LOG.error("Problem invoking callback", e);
        onException(e);
      } finally {
        map.remove(Context.getCorrelationId());
      }
    }
  }

  /**
   * Invoked when a callback fails. By default exception is ignored.
   *
   * @param e
   *          {@link Exception} thrown by the fallback.
   */
  protected void onException(final Exception e) {
  }
}
