/*
 * Copyright (C) 2010 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.StopWatch;

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
  public String create(final String content) {
    if (content == null) {
      throw new IllegalArgumentException("Content cannot be null!");
    }
    return getMD5Hash(content.getBytes());
  }

  /**
   * Computes md5 hash.
   *
   * @param bytes used for hashing.
   * @return 32 bytes hash.
   */
  public String getMD5Hash(final byte[] bytes) {
    LOG.debug("Computing hash");
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("md5 digest");
    final StringBuilder hash = new StringBuilder();
    try {
      final MessageDigest m = MessageDigest.getInstance("MD5");
      m.update(bytes);
      stopWatch.stop();
      stopWatch.start("compute hash");
      final byte data[] = m.digest();
      for (final byte element : data) {
        hash.append(Character.forDigit((element >> 4) & 0xf, 16));
        hash.append(Character.forDigit(element & 0xf, 16));
      }
      return hash.toString();
    } catch (final NoSuchAlgorithmException e) {
      throw new WroRuntimeException("Exception occured while computing md5 hash", e);
    } finally {
      stopWatch.stop();
      LOG.debug("hash: " + hash.toString());
      LOG.debug("hash computation took: " + stopWatch.prettyPrint());
    }
  }
}
