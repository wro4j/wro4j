package ro.isdc.wro.http.handler;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;


/**
 * Triggers resource watcher check.
 *
 * @author Alex Objelean
 * @created 9 Jan 2014
 * @since 1.7.3
 */
public class ResourceWatcherRequestHandler
    extends RequestHandlerSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcherRequestHandler.class);
  private static final String PARAM_GROUP_NAME = "groupName";
  private static final String PARAM_RESOURCE_TYPE = "resourceType";
  private static final String PATH_HANDLER = "resourceWatch";
  /**
   * The alias of this {@link RequestHandler} used for configuration.
   */
  public static final String ALIAS = "resourceWatcher";

  @Inject
  private ResourceWatcher resourceWatcher;

  @Inject
  private ReadOnlyContext context;

  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    resourceWatcher.check(retrieveCacheKey(request));
  }


  public static void check(final CacheKey cacheKey, final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    notNull(cacheKey);
    notNull(request);
    notNull(response);
    final String location = getRequestHandlerPath(cacheKey.getGroupName(), cacheKey.getType());
    new DispatcherStreamLocator().getInputStream(request, response, location);
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
    // this request handler is forbidden to avoid .
    final boolean isDispatchedRequest = DispatcherStreamLocator.isIncludedRequest(request);
    final boolean isHandlerRequest = isHandlerUri(request.getRequestURI());
    return isHandlerRequest && isDispatchedRequest;
  }


  /**
   * Checks if the provided url is a resource proxy request.
   * @param url
   *          to check.
   * @return true if the provided url is a proxy resource.
   */
  private boolean isHandlerUri(final String url) {
    notNull(url);
    return url.contains(getRequestHandlerPath());
  }


  static String getRequestHandlerPath() {
    return String.format("%s/%s", PATH_API, PATH_HANDLER);
  }

  static String getRequestHandlerPath(final String groupName, final ResourceType resourceType) {
    return String.format("%s?%s=%s&%s=%s", getRequestHandlerPath(), PARAM_GROUP_NAME, groupName, PARAM_RESOURCE_TYPE, resourceType.name());
  }
}