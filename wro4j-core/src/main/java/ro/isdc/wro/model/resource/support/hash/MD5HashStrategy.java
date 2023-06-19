package ro.isdc.wro.model.resource.support.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Uses MD5 algorithm for creating fingerprint.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class MD5HashStrategy
    extends AbstractDigesterHashStrategy {
  /**
   * A short name of this hashBuilder.
   */
  public static final String ALIAS = "MD5";
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected MessageDigest newMessageDigest()
      throws NoSuchAlgorithmException {
    return MessageDigest.getInstance(ALIAS);
  }
}
