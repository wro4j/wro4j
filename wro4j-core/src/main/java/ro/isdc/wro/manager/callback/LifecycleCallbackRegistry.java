/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.manager.callback;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;


/**
 * Register all available callbacks. The registry acts as a {@link LifecycleCallback} itself whose implementation
 * delegate the call to registered callbacks.
 *
 * @author Alex Objelean
 * @created Created on 8 Dec 2011
 * @since 1.4.3
 */
public class LifecycleCallbackRegistry implements LifecycleCallback {
  /**
   * The list of registered callbacks.
   */
  private List<LifecycleCallback> callbacks = new ArrayList<LifecycleCallback>();

  /**
   * @param callback to register.
   */
  public void registerCallback(final LifecycleCallback callback) {
    Validate.notNull(callback);
    callbacks.add(callback);
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforeModelCreated() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onBeforeModelCreated();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onAfterModelCreated();
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcess() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onBeforePreProcess();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onAfterPreProcess();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onBeforePostProcess();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcess() {
    for (final LifecycleCallback callback : callbacks) {
      callback.onAfterPostProcess();
    }
  }
}
