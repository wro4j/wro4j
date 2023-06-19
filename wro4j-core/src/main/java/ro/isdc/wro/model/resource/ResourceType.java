/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * Make a distinction between resource type. Can be CSS or JS.
 *
 * @author Alex Objelean
 */
public enum ResourceType {
  CSS {
    @Override
    public String getContentType() {
      return "text/css";
    }
  },
  JS {
    @Override
    public String getContentType() {
      return "text/javascript";
    }
  };
  /**
   * @return the content type of the resource type.
   */
  public abstract String getContentType();

  /**
   * @return {@link ResourceType} associated to the string representation of the type.
   */
  public static ResourceType get(final String typeAsString) {
    isTrue(typeAsString != null, "ResourceType cannot be NULL.");
    return ResourceType.valueOf(typeAsString.toUpperCase());
  }
}
