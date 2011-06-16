/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make a distinction between resource type. Can be CSS or JS.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
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
  private static final Logger LOG = LoggerFactory.getLogger(ResourceType.class);
  /**
   * @return the content type of the resource type.
   */
  public abstract String getContentType();

  /**
   * @return {@link ResourceType} associated to the string representation of the type.
   */
  public static ResourceType get(final String typeAsString) {
    return ResourceType.valueOf(typeAsString.toUpperCase());
  }

  /**
   * Same as {@link ResourceType#get(String)} but will never return an {@link IllegalArgumentException}. When an invalid
   * resource type is searched, a default one will be used.
   *
   * @param typeAsString
   * @return
   */
  public static ResourceType getSafe(final String typeAsString) {
    ResourceType type = ResourceType.CSS;
    try {
      type = get(typeAsString);
    } catch(final IllegalArgumentException e) {
      LOG.debug("Invalid type found: " + typeAsString + ". Falling back to default one: " + type);
    }
    return type;
  }
}
