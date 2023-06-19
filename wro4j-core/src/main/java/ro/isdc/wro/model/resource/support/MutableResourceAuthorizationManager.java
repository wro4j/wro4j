package ro.isdc.wro.model.resource.support;

/**
 * A specialized {@link ResourceAuthorizationManager} which is capable of changing the authorized resource in the
 * runtime.
 * 
 * @author Alex Objelean
 */
public interface MutableResourceAuthorizationManager extends ResourceAuthorizationManager {
  /**
   * Add a new resource uri to the set of authorized resources.
   * 
   * @param uri
   *          the resource uri to authorize.
   */
  void add(final String uri);
  
  /**
   * Clear all authorized resources added previously.
   */
  void clear();
}
