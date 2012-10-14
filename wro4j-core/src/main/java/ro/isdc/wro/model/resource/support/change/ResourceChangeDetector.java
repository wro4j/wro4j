package ro.isdc.wro.model.resource.support.change;


import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * @author Alex Objelean
 * @since 1.5.1
 * @created 14 Oct 2012
 */
public class ResourceChangeDetector {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceChangeDetector.class);
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private HashStrategy hashStrategy;
  /**
   * Map between a resource uri and a corresponding {@link ResourceChangeInfo} object.
   */
  private final Map<String, ResourceChangeInfo> changeInfoMap = new ConcurrentHashMap<String, ResourceChangeInfo>() {
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

  public void cleanUp() {
    for (final ResourceChangeInfo resourceInfo : changeInfoMap.values()) {
      resourceInfo.reset();
    }
  }

  public boolean checkChangeForGroup(final String uri, final String groupName) throws IOException {
    notNull(uri);
    notNull(groupName);
    LOG.debug("checkChange of uri: {}, for group: {}", uri, groupName);
    final ResourceChangeInfo resourceInfo = changeInfoMap.get(uri);
    if (resourceInfo.isCheckRequiredForGroup(groupName)) {
      final String currentHash = hashStrategy.getHash(locatorFactory.locate(uri));
      resourceInfo.updateHashForGroup(currentHash, groupName);
    }
    return resourceInfo.isChanged(groupName);
  }
}
