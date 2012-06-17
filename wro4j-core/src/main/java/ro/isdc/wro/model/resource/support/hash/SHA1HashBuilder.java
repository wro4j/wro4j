/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.support.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Uses SHA1 algorithm for creating fingerprint.
 * 
 * @author Alex Objelean
 * @deprecated use {@link SHA1HashStrategy} instead.
 */
public class SHA1HashBuilder
    extends AbstractDigesterHashStrategy {
  /**
   * A short name of this strategy.
   */
  public static final String ALIAS = "SHA-1";
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected MessageDigest newMessageDigest()
      throws NoSuchAlgorithmException {
    return MessageDigest.getInstance(ALIAS);
  }
}
