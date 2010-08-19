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
 * Uses SHA1 algorithm for creating fingerprint.
 *
 * @author Alex Objelean
 */
public abstract class AbstractDigesterFingerprintCreator
  implements FingerprintCreator {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDigesterFingerprintCreator.class);


  /**
   * {@inheritDoc}
   */
  public String create(final InputStream input)
    throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("Content cannot be null!");
    }
    try {
      LOG.debug("creating hash using SHA1 algorithm");
      final MessageDigest messageDigest = newMessageDigest();
      final InputStream digestIs = new DigestInputStream(input, messageDigest);
      // read till the end
      while (digestIs.read() != -1) {
      }
      final byte[] digest = messageDigest.digest();
      final String hash = new BigInteger(1, digest).toString(16);

      LOG.debug(getClass().getSimpleName() + " hash: " + hash);
      return hash;
    } catch (final NoSuchAlgorithmException e) {
      throw new WroRuntimeException("Exception occured while computing SHA1 hash", e);
    }
  }


  /**
   * @return
   * @throws NoSuchAlgorithmException
   */
  protected abstract MessageDigest newMessageDigest() throws NoSuchAlgorithmException;
}
