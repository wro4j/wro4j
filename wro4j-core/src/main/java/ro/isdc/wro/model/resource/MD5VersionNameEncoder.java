/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

import org.apache.commons.io.FilenameUtils;

/**
 * Encodes md5 fingerprint hash into the name.
 * For instance:
 * For a resource named resource.js => resource-920348029834.js
 *
 * @author Alex Objelean
 */
public class MD5VersionNameEncoder
    implements VersionNameEncoder {
  private final FingerprintCreator fingerprintCreator = new MD5FingerprintCreator();
  /**
   * {@inheritDoc}
   */
  public String encode(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null!");
    }
    final String oldName = FilenameUtils.getName(name);

    return oldName;
  }
}
