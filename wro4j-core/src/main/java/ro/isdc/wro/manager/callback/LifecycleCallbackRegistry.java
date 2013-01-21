/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.manager.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Register all available callbacks. The registry acts as a {@link LifecycleCallback} itself whose implementation
 * delegate the call to registered callbacks. The registry will handle any runtime exceptions thrown by callbacks, in
 * order to allow successful lifecycle execution.
 *
 * @author Alex Objelean
 * @created Created on 8 Dec 2011
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
   * @deprecated use {@link #registerCallback(ObjectFactory)} instead to ensure thread-safety.
   * @param callback to register.
   */
  @Deprecated
  public void registerCallback(final LifecycleCallback callback) {
    Validate.notNull(callback);
    registerCallback(new ObjectFactory<LifecycleCallback>() {
      public LifecycleCallback create() {
        return callback;
      }
    });
  }

  /**
   * Register a callback using a factory responsible for callback instantiation.
   * @param callbackFactory the factory used to instantiate callbacks.
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
  public void onBeforeModelCreated() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onBeforeModelCreated();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforeModelCreated", e);
        onException(e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onAfterModelCreated();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterModelCreated", e);
        onException(e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcess() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onBeforePreProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforePreProcess", e);
        onException(e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onAfterPreProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterPreProcess", e);
        onException(e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onBeforePostProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforePostProcess", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcess() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onAfterPostProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterPostProcess", e);
        onException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforeMerge() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onBeforeMerge();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforeMerge", e);
        onException(e);
      }
    }
  }

  public void onAfterMerge() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onAfterMerge();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterMerge", e);
        onException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onProcessingComplete() {
    for (final LifecycleCallback callback : getCallbacks()) {
      try {
        callback.onProcessingComplete();
      } catch (final Exception e) {
        LOG.error("Problem invoking onProcessingComplete", e);
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
