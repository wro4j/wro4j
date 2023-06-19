/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

/**
 * Mark processors implementing this interface as minimize aware. This is an alternative for using {@code Minimize}
 * annotation.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public interface MinimizeAware {
  /**
   * @return true if the processor implementing this method performs some kind of minification.
   */
  boolean isMinimize();
}
