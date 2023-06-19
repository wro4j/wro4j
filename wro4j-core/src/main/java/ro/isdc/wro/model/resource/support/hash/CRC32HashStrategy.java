package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses CRC32 algorithm for creating fingerprint.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class CRC32HashStrategy
    implements HashStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(CRC32HashStrategy.class);
  /**
   * A short name of this hashBuilder.
   */
  public static final String ALIAS = "CRC32";
  /**
   * {@inheritDoc}
   */
  @Override
  public String getHash(final InputStream input)
    throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("Content cannot be null!");
    }
    try (input) {
      LOG.debug("creating hash using CRC32 algorithm");
      final Checksum checksum = new CRC32();
      final byte[] bytes = new byte[1024];
      int len = 0;
      while ((len = input.read(bytes)) >= 0) {
        checksum.update(bytes, 0, len);
      }

      final String hash = new BigInteger(Long.toString(checksum.getValue())).toString(16);
      LOG.debug("CRC32 hash: {}", hash);
      return hash;
    }
  }
}
