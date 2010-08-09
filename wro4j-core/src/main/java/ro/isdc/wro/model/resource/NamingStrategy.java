/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

/**
 * Used to version a resource name somehow. There could be many strategies, like timestamp, content hashing, etc.
 *
 * @author Alex Objelean
 */
public interface NamingStrategy {
  /**
   * Creates a new name of the resource which encodes a version.
   *
   * @param name old name of the resource.
   * @return new name of the resource with version encoded.
   */
  public String rename(final String name);
}
