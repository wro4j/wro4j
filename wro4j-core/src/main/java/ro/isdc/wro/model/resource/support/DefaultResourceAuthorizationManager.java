package ro.isdc.wro.model.resource.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;
import ro.isdc.wro.util.WroUtil;


/**
 * Controls the resources which should be accessible through {@link ResourceProxyRequestHandler}. Prevent the security
 * issue which may occur when everything can be accessed using the proxy.
 *
 * @author Alex Objelean
 */
public class DefaultResourceAuthorizationManager implements MutableResourceAuthorizationManager {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceAuthorizationManager.class);

  private final Set<String> authorizedResources = Collections.synchronizedSet(new HashSet<String>());

  public boolean isAuthorized(final String uri) {
    return authorizedResources.contains(WroUtil.removeQueryString(uri));
  }

  public void add(final String uri) {
    notNull(uri);
    LOG.debug("authorize: {}", uri);
    //ignore query string added to authorized resources list.
    authorizedResources.add(WroUtil.removeQueryString(uri));
  }

  /**
   * @return a read-only copy of authorized resources.
   */
  public Collection<String> list() {
    return Collections.unmodifiableCollection(authorizedResources);
  }

  public void clear() {
    LOG.debug("clear.");
    authorizedResources.clear();
  }
}
