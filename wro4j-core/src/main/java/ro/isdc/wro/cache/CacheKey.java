package ro.isdc.wro.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
   * Add a custom key-value pair attribute. Each pair is added to an internal map. The custom attributes can be used to
   * make the key more fine grained (Ex: based on browser version or a request parameter). Both elements of the
   * attribute (key & value) should be not null. If any of these are null, the attribute won't be added.
   *
   * @param key
   *          string representing the key of the attribute.
   * @param value
   *          string representing the value of the attribute.
   * @return reference to current {@link CacheKey} used for fluent interface.
   */
  public CacheKey addAttribute(final String key, final String value) {
    if (key != null && value != null) {
      map.put(key, value);
    }
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}