package ro.isdc.wro.model.resource.support.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Uses SHA1 algorithm for creating fingerprint.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class SHA1HashStrategy
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
