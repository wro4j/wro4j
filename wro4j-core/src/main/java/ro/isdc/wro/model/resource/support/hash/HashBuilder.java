package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alex Objelean
 * @deprecated use {@link HashStrategy} instead.
 */
public interface HashBuilder {
  /**
   * @param inputStream to digest.
   * @return the hash of the content.
   * @throws IOException if there was an error during reading the stream content.
   */
  public String getHash(final InputStream inputStream) throws IOException;
}
