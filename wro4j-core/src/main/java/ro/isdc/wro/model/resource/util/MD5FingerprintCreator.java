/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Uses MD5 algorithm for creating fingerprint.
 *
 * @author Alex Objelean
 */
public class MD5FingerprintCreator
  implements FingerprintCreator {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MD5FingerprintCreator.class);


  /**
   * {@inheritDoc}
   */
  public String create(final InputStream input)
    throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("Content cannot be null!");
    }
    return getMD5Hash(input);
  }


  /**
   * Computes md5 hash.
   *
   * @param bytes used for hashing.
   * @return 32 bytes hash.
   */
  private String getMD5Hash(final InputStream input)
    throws IOException {
    try {
      LOG.debug("Computing hash");
      final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      final InputStream digestIs = new DigestInputStream(input, messageDigest);
      // read entire stream
      while (digestIs.read() != -1) {
      }
      final byte[] digest = messageDigest.digest();
      final String hash = new BigInteger(1, digest).toString(16);
      LOG.debug("MD5 computed hash: " + hash);
      return hash;
    } catch (final NoSuchAlgorithmException e) {
      throw new WroRuntimeException("Exception occured while computing md5 hash", e);
    }
  }
}
