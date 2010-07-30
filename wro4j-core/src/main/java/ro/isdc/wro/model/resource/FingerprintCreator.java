/*
 * Copyright (C) 2010 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

/**
 * Creates a fingerprint representation of content.
 *
 * @author Alex Objelean
 */
public interface FingerprintCreator {
  /**
   * @param content to digest.
   * @return the hash of the content.
   */
  public String create(final String content);
}
