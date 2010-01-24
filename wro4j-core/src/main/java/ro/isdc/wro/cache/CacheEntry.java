package ro.isdc.wro.cache;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ro.isdc.wro.resource.ResourceType;

/**
 * Used as an entry for the cache.
 */
@SuppressWarnings("serial")
public class CacheEntry implements Serializable {
  private ResourceType type;
  private String groupName;
  public CacheEntry(final String groupName, final ResourceType type) {
    this.groupName = groupName;
    this.type = type;
  }
  /**
   * @return the type
   */
  public ResourceType getType() {
    return this.type;
  }
  /**
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
}