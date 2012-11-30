package ro.isdc.wro.cache.factory;

import static org.apache.commons.lang3.Validate.notNull;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Default implementation of {@link CacheKeyFactory} which builds the {@link CacheKey} by setting all required types
 * after inspecting the request. The {@link GroupExtractor} is used to retrieve required fields.
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @created 19 Oct 2012
 */
public class DefaultCacheKeyFactory
    implements CacheKeyFactory {
  @Inject
  private GroupExtractor groupExtractor;

  /**
   * {@inheritDoc}
   */
  public CacheKey create(final HttpServletRequest request) {
    notNull(request);
    CacheKey key = null;
    final String groupName = groupExtractor.getGroupName(request);
    final ResourceType resourceType = groupExtractor.getResourceType(request);
    final boolean minimize = groupExtractor.isMinimized(request);
    if (groupName != null && resourceType != null) {
      key = new CacheKey(groupName, resourceType, minimize);
    }
    return key;
  }
}
