package ro.isdc.wro.config.metadata;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of {@link MetaDataFactory} which returns always a map created during construction.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public class DefaultMetaDataFactory
    implements MetaDataFactory {
  private final Map<String, Object> metadata;

  /**
   * Creates a factory with empty metadata map.
   */
  public DefaultMetaDataFactory() {
    this(new HashMap<String, Object>());
  }

  /**
   * Creates a factory which holds the provided map as metadata storage.
   * @param metadata a not null map of metadata.
   */
  public DefaultMetaDataFactory(final Map<String, Object> metadata) {
    notNull(metadata);
    this.metadata = metadata;
  }

  /**
   * @return the unmodifiable version of the metadata.
   */
  public Map<String, Object> create() {
    return Collections.unmodifiableMap(metadata);
  }
}
