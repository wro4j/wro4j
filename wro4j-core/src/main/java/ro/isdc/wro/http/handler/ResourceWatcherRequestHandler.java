package ro.isdc.wro.http.handler;

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;


/**
 * Triggers resource watcher check. This handler is not meant to be invoked publicly. It has a protection mechanism,
 * which allows only server-side handling.
 *
 * @author Alex Objelean
 * @since 1.7.3
 */
public class ResourceWatcherRequestHandler
    extends RequestHandlerSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcherRequestHandler.class);
  /**
   * @VisibleForTesting
   */
  static final String PARAM_GROUP_NAME = "group";
  /**
   * @VisibleForTesting
   */
  static final String PARAM_RESOURCE_TYPE = "resourceType";
  /**
   * @VisibleForTesting
   */
  static final String PARAM_AUTH_KEY = "auth";
  static final String PATH_HANDLER = "resourceWatcher";
  /**
   * The alias of this {@link RequestHandler} used for configuration.
   */
  public static final String ALIAS = "resourceWatcher";
  @Inject
  private ResourceWatcher resourceWatcher;
  /**
   * A random string used to authorize request. This key is updated after each successful handle operation to avoid
   * hijacking.
   */
  private static String authorizationKey;

  public ResourceWatcherRequestHandler() {
    updateAuthorizationKey();
  }

  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    resourceWatcher.check(retrieveCacheKey(request));
    updateAuthorizationKey();
  }

  private CacheKey retrieveCacheKey(final HttpServletRequest request) {
    CacheKey cacheKey = null;
    final String resourceTypeAsString = request.getParameter(PARAM_RESOURCE_TYPE);
    final String groupName = request.getParameter(PARAM_GROUP_NAME);
    try {
      final ResourceType resourceType = ResourceType.get(resourceTypeAsString);
      isTrue(groupName != null);
      if (groupName != null) {
        LOG.debug("groupName={}, resourceType={}", groupName, resourceType);
        cacheKey = new CacheKey(groupName, resourceType);
      }
    } catch (final IllegalArgumentException e) {
      LOG.debug("groupName={}, resourceType={}", groupName, resourceTypeAsString);
      throw WroRuntimeException.wrap(e, "Cannot retrieve cacheKey from the request");
    }
    return cacheKey;
  }

  @Override
  public boolean accept(final HttpServletRequest request) {
    // Authorize only server-side included request (performed by {@link DispatcherStreamLocator}). Any public access to
    // this request handler is forbidden.
    final boolean isHandlerRequest = isHandlerRequest(request);
    return isHandlerRequest && isAuthorized(request);
  }

  private boolean isAuthorized(final HttpServletRequest request) {
    final String actualKey = request.getParameter(PARAM_AUTH_KEY);
    final boolean isAuthorized = authorizationKey.equals(actualKey);
    if (!isAuthorized) {
      LOG.debug("Unauthorized request. actualKey={}, expected={}", actualKey, authorizationKey);
    }
    return isAuthorized;
  }

  /**
   * Checks if the provided url is a resource proxy request.
   *
   * @param url
   *          to check.
   * @return true if the provided url is a proxy resource.
   */
  private boolean isHandlerRequest(final HttpServletRequest request) {
    String apiHandlerValue = request.getParameter(PATH_API);
    return PATH_HANDLER.equals(apiHandlerValue) && retrieveCacheKey(request) != null;
  }

  /**
   * Updates the authorizationKey with an unique value.
   */
  private void updateAuthorizationKey() {
    authorizationKey = generateRandomKey();
  }

  /**
   * @VisibleForTesting
   * @return a random key used for authorization.
   */
  String generateRandomKey() {
    return UUID.randomUUID().toString();
  }

  /**
   * Computes the servlet context relative url to call this handler using a server-side invocation. Hides the details
   * about creating a valid url and providing the authorization key required to invoke this handler.
   */
  public static String createHandlerRequestPath(final CacheKey cacheKey, final HttpServletRequest request) {
    final String handlerQueryPath = getRequestHandlerPath(cacheKey.getGroupName(), cacheKey.getType());
    return request.getServletPath() + handlerQueryPath;
  }

  private static String getRequestHandlerPath() {
    return String.format("?%s=%s", PATH_API, PATH_HANDLER);
  }

  private static String getRequestHandlerPath(final String groupName, final ResourceType resourceType) {
    return String.format("%s&%s=%s&%s=%s&%s=%s", getRequestHandlerPath(), PARAM_GROUP_NAME, groupName,
        PARAM_RESOURCE_TYPE, resourceType.name(), PARAM_AUTH_KEY, authorizationKey);
  }
}