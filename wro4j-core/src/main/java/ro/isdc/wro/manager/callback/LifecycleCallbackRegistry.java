/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.manager.callback;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
      try {
        callback.onBeforeModelCreated();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforeModelCreated", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onAfterModelCreated();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterModelCreated", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcess() {
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onBeforePreProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforePreProcess", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onAfterPreProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterPreProcess", e);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
    for (final LifecycleCallback callback : callbacks) {
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
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onAfterPostProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterPostProcess", e);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void onBeforeProcess() {
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onBeforeProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onBeforeProcess", e);
      }
    }    
  }
  
  /**
   * {@inheritDoc}
   */
  public void onAfterProcess() {
    for (final LifecycleCallback callback : callbacks) {
      try {
        callback.onAfterProcess();
      } catch (final Exception e) {
        LOG.error("Problem invoking onAfterProcess", e);
      }
    }    
  }
}
