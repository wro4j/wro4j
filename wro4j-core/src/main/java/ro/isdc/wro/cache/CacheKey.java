package ro.isdc.wro.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * Used as an entry for the cache.
 */
@SuppressWarnings("serial")
public final class CacheKey implements Serializable {
	private final ResourceType type;
	private final String groupName;
	/**
	 * Produce minimized version.
	 */
	private final boolean minimize;
	/**
	 * A map of custom attributes.
	 */
	private final Map<String, String> map = new HashMap<String, String>();

	/**
	 *
	 * @param groupName name of the group.
	 * @param type resource type (js or css)
	 * @param minimize true if the result should produce minimized version.
	 */
	public CacheKey(final String groupName, final ResourceType type, final boolean minimize) {
	  Validate.notNull(groupName);
	  Validate.notNull(type);
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
   * Add a custom attribute represented as a key-value pair. Each pair is added to an internal map. The custom
   * attributes can be used to make the key more fine grained (Ex: based on browser version or a request parameter).
   *
   * @param key
   *          not null string representing the key of the attribute.
   * @param value
   *          not null string representing the value of the attribute.
   * @return reference to current {@link CacheKey} used for fluent interface.
   */
  public CacheKey addAttribute(final String key, final String value) {
    Validate.notNull(key);
    Validate.notNull(value);
    map.put(key, value);
    return this;
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
	  return HashCodeBuilder.reflectionHashCode(this, false);
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public String toString() {
    return new ToStringBuilder("").append(getGroupName()).append(getType()).append(isMinimize()).toString();
  }
}