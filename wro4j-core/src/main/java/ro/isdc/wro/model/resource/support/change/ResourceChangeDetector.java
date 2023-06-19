package ro.isdc.wro.model.resource.support.change;


import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;


/**
 * Responsbile for keeping details about resources contents and identify the change of any resource in time.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public class ResourceChangeDetector {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceChangeDetector.class);
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private HashStrategy hashStrategy;
  /**
   * Map between a resource uri and a corresponding {@link ResourceChangeInfo} object. It is ensured that any get(key)
   * operation will return a not null object.
   */
  private final Map<String, ResourceChangeInfo> changeInfoMap = new ConcurrentHashMap<>() {
	private static final long serialVersionUID = 1L;

	@Override
    public ResourceChangeInfo get(final Object key) {
      ResourceChangeInfo result = super.get(key);
      if (result == null) {
        result = new ResourceChangeInfo();
        put((String) key, result);
      }
      return result;
    }
  };

  /**
   * Notifies the {@link ResourceChangeDetector} that the change cycle completes and a new one is prepared.
   */
  public void reset() {
    for (final ResourceChangeInfo resourceInfo : changeInfoMap.values()) {
      resourceInfo.reset();
    }
  }

  /**
   * Check if an uri from a particular group has changed.
   *
   * @param uri
   *          the uri to check for change.
   * @param groupName
   *          the name of the group where the uri belongs to.
   * @return true if the change is detected for the provided uri.
   */
  public boolean checkChangeForGroup(final String uri, final String groupName) throws IOException {
    notNull(uri);
    notNull(groupName);
    LOG.debug("group={}, uri={}", groupName, uri);
    final ResourceChangeInfo resourceInfo = changeInfoMap.get(uri);
    if (resourceInfo.isCheckRequiredForGroup(groupName)) {
      final InputStream inputStream = locatorFactory.locate(uri);
      try (inputStream) {
        final String currentHash = hashStrategy.getHash(inputStream);
        resourceInfo.updateHashForGroup(currentHash, groupName);
      }
    }
    return resourceInfo.isChanged(groupName);
  }
}
