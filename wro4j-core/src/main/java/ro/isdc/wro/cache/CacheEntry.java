package ro.isdc.wro.cache;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * Used as an entry for the cache.
 */
@SuppressWarnings("serial")
public final class CacheEntry implements Serializable {
	private final ResourceType type;
	private final String groupName;
	/**
	 * Produce minimized version.
	 */
	private boolean minimize;

	/**
	 *
	 * @param groupName name of the group.
	 * @param type resource type (js or css)
	 * @param minimize true if the result should produce minimized version.
	 */
	public CacheEntry(final String groupName, final ResourceType type, final boolean minimize) {
		this.groupName = groupName;
		this.type = type;
		this.minimize = minimize;
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


	/**
   * @return the minimize
   */
  public boolean isMinimize() {
    return this.minimize;
  }


  /**
   * @param minimize the minimize to set
   */
  public void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }


  /**
   * {@inheritDoc}
   */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	  return ToStringBuilder.reflectionToString(this);
	}
}