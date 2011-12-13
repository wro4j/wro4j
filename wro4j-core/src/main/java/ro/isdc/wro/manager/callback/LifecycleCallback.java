/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

/**
 * Defines callbacks invoked by the manager during processing. Any of the lifecycle method can throw a
 * {@link RuntimeException} which will be handled properly by {@link LifecycleCallbackRegistry}.
 *
 * @author Alex Objelean
 * @created 26 Oct 2011
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
   * Called before the first preProcessor is used.
   */
  void onBeforeProcess();
  
  /**
   * Called after the last postProcessor is used.
   */
  void onAfterProcess();
}
