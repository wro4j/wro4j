package ro.isdc.wro.model.resource.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;


/**
 * Controls the resources which should be accessible through {@link ResourceProxyRequestHandler}. Prevent the security
 * issue which may occur when everything can be accessed using the proxy.
 * 
 * @author Alex Objelean
 */
public class ResourceAuthorizationManager {
  private final Set<String> authorizedResources = Collections.synchronizedSet(new HashSet<String>());

  /**
   * @param uri
   *          of the resource to authorize.
   * @return true if the provided uri is an authorized resource.
   */
  public boolean isAuthorized(final String uri) {
    return authorizedResources.contains(uri);
  }
  
  /**
   * Add a new resource uri to the set of authorized resources.
   * 
   * @param uri
   *          the resource uri to authorize.
   */
  public void add(final String uri) {
    Validate.notNull(uri);
    authorizedResources.add(uri);
  }
  
  /**
   * Clear all authorized resources added previously.
   */
  public void clear() {
    authorizedResources.clear();
  }
}
