/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Uses SHA1 algorithm for creating fingerprint.
 *
 * @author Alex Objelean
 */
public class SHA1FingerprintCreator extends AbstractDigesterFingerprintCreator {
  /**
   * {@inheritDoc}
   */
  @Override
  protected MessageDigest newMessageDigest()
    throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("SHA-1");
  }
}
