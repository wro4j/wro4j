/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;


/**
 * Creates a fingerprint representation of the resource content.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public interface HashStrategy {
  /**
   * @param inputStream to digest.
   * @return the hash of the content.
   * @throws IOException if there was an error during reading the stream content.
   */
  public String getHash(final InputStream inputStream) throws IOException;
}
