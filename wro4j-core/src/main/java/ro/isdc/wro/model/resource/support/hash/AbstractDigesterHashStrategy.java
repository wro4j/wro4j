/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Uses abstract digester for creating a hash.
 *
 * @author Alex Objelean
 */
public abstract class AbstractDigesterHashStrategy
  implements HashStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDigesterHashStrategy.class);


  public String getHash(final InputStream input)
    throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("Content cannot be null!");
    }
    try {
      final MessageDigest messageDigest = newMessageDigest();
      final InputStream digestIs = new DigestInputStream(input, messageDigest);
      // read till the end
      while (digestIs.read() != -1) {
      }
      final byte[] digest = messageDigest.digest();
      final String hash = new BigInteger(1, digest).toString(16);

      LOG.debug("{} hash: {}", getClass().getSimpleName(), hash);
      return hash;
    } catch (final NoSuchAlgorithmException e) {
      throw new WroRuntimeException("Exception occured while computing SHA1 hash", e);
    }finally{
      IOUtils.closeQuietly(input);
    }
  }


  /**
   * @return MessageDigest used for hashing.
   * @throws NoSuchAlgorithmException
   */
  protected abstract MessageDigest newMessageDigest() throws NoSuchAlgorithmException;
}
