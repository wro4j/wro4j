/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.manager.callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * Register all available callbacks.
 *
 * @author Alex Objelean
 * @created Created on 8 Dec 2011
 * @since 1.4.3
 */
public class LifecycleCallbackRegistry {
  private List<LifecycleCallback> callbacks = new ArrayList<LifecycleCallback>();
  
  /**
   * @param callback to register.
   */
  public void registerCallback(final LifecycleCallback callback) {
    Validate.notNull(callback);
    callbacks.add(callback);
  }
  
  /**
   * @return a readonly collection of the registered callbacks.
   */
  public Collection<LifecycleCallback> getCallbacks() {
    return Collections.unmodifiableCollection(callbacks);
  }
}
