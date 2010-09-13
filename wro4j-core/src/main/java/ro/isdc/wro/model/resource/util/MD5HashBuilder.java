/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Uses MD5 algorithm for creating fingerprint.
 *
 * @author Alex Objelean
 */
public class MD5FingerprintCreator
  extends AbstractDigesterFingerprintCreator {
  /**
   * {@inheritDoc}
   */
  @Override
  protected MessageDigest newMessageDigest()
    throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("MD5");
  }
}
