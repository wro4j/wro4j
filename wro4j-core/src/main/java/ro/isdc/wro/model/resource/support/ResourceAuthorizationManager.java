package ro.isdc.wro.model.resource.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;


/**
 * Controls the resources which should be accessible through {@link ResourceProxyRequestHandler}. Prevent the security
 * issue which may occur when everything can be accessed using the proxy.
 * 
 * @author Alex Objelean
 */
public class ResourceAuthorizationManager {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceAuthorizationManager.class);

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
    LOG.debug("authorize resource: {}", uri);
    authorizedResources.add(uri);
  }
  
  /**
   * @return a read-only copy of authorized resources.
   */
  public Collection<String> list() {
    return Collections.unmodifiableCollection(authorizedResources);
  }
  
  /**
   * Clear all authorized resources added previously.
   */
  public void clear() {
    LOG.debug("clear authorized resources.");
    authorizedResources.clear();
  }
}
