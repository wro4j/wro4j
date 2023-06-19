package ro.isdc.wro.model.resource.processor;

/**
 * Processors may optionally implement this interface to provide the capability to destroy its contents.
 *
 * @author Alex Objelean
 * @since 1.7.1
 */
public interface Destroyable {
  /**
   * Destroy this object or any other internal state created during initialization.
   *
   * @throws Exception if the destroy operation failed.
   */
  void destroy() throws Exception;
}
