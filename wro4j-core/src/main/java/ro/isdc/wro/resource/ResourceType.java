/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.resource;

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
  /**
   * @return the content type of the resource type.
   */
  public abstract String getContentType();
}
