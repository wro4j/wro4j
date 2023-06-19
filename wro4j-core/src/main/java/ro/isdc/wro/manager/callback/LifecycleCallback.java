/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;

/**
 * Defines callbacks invoked by the manager during processing. Any of the lifecycle method can throw a
 * {@link RuntimeException} which will be handled properly by {@link LifecycleCallbackRegistry}.
 *
 * @author Alex Objelean
 * @since 1.4.3
 */
public interface LifecycleCallback {
  /**
   * Invoked before starting model creation.
   */
  void onBeforeModelCreated();

  /**
   * Invoked after the model is created.
   */
  void onAfterModelCreated();

  /**
   * Called before each resource is processed.
   */
  void onBeforePreProcess();

  /**
   * Called after a resource is pre processed.
   */
  void onAfterPreProcess();

  /**
   * Called before a resource is post processed.
   */
  void onBeforePostProcess();

  /**
   * Called after a resource is post processed.
   */
  void onAfterPostProcess();

  /**
   * Called before resources are merged and before any processing is applied.
   */
  void onBeforeMerge();

  /**
   * Called after all resources are merged and the preProcessing is completed.
   */
  void onAfterMerge();
  /**
   * Called after all postProcessors are applied and overall processing is complete.
   */
  void onProcessingComplete();

  /**
   * Called when the provided resource change has been detected by {@link ResourceWatcher}.
   *
   * @param resource
   *          {@link Resource} whose change was detected.
   */
  void onResourceChanged(Resource resource);
}
