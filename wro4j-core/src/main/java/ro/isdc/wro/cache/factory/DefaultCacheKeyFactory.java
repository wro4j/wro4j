package ro.isdc.wro.cache.factory;

import static org.apache.commons.lang3.Validate.notNull;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Default implementation of {@link CacheKeyFactory} which builds the {@link CacheKey} by setting all required types
 * after inspecting the request. The {@link GroupExtractor} is used to retrieve required fields. The minimize flag will
 * be computed based on {@link WroConfiguration#isMinimizeEnabled()} configuration.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public class DefaultCacheKeyFactory
    implements CacheKeyFactory {
  @Inject
  private GroupExtractor groupExtractor;
  @Inject
  private ReadOnlyContext context;

  /**
   * {@inheritDoc}
   */
  public CacheKey create(final HttpServletRequest request) {
    notNull(request);
    CacheKey key = null;
    final String groupName = groupExtractor.getGroupName(request);
    final ResourceType resourceType = groupExtractor.getResourceType(request);
    final boolean minimize = isMinimized(request);
    if (groupName != null && resourceType != null) {
      key = new CacheKey(groupName, resourceType, minimize);
    }
    return key;
  }

  /**
   * Uses isMinimizeEnabled configuration to compute minimize value.
   */
  private boolean isMinimized(final HttpServletRequest request) {
    return context.getConfig().isMinimizeEnabled() ? groupExtractor.isMinimized(request) : false;
  }
}
