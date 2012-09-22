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
public class DefaultResourceAuthorizationManager implements MutableResourceAuthorizationManager {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceAuthorizationManager.class);

  private final Set<String> authorizedResources = Collections.synchronizedSet(new HashSet<String>());

  /**
   * {@inheritDoc}
   */
  public boolean isAuthorized(final String uri) {
    return authorizedResources.contains(uri);
  }
  
  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
   */
  public void clear() {
    LOG.debug("clear authorized resources.");
    authorizedResources.clear();
  }
}
