/*
 * Copyright (C) 2010 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
  public String create(final InputStream input) throws IOException {
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
  public String getMD5Hash(final InputStream input) throws IOException {
    LOG.debug("Computing hash");
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("md5 digest");
    final StringBuilder hash = new StringBuilder();
    final InputStream bis = new BufferedInputStream(input);
    try {
      final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      byte result = 0;
      do {
        result = (byte)bis.read();
        messageDigest.update(result);
      } while(result != -1);
      stopWatch.stop();
      stopWatch.start("compute hash");
      final byte data[] = messageDigest.digest();
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
