/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.support.naming;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used to version a resource name somehow. There could be many strategies, like timestamp, content hashing, etc.
 *
 * @author Alex Objelean
 */
public interface NamingStrategy {
  /**
   * Creates a new name of the resource which encodes a version.
   *
   * @param originalName original name of the resource.
   * @param inputStream the stream of the content to rename.
   * @return new name of the resource with version encoded.
   * @throws IOException if there are stream reading problem.
   */
  public String rename(final String originalName, final InputStream inputStream) throws IOException;
}
