/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.cache;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;


/**
 * Entry holding a resource content along with its associated hash.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public final class ContentHashEntry
  implements Serializable {
  private String rawContent;
  private byte[] gzippedContent;
  private String hash;

  private ContentHashEntry(final String content, final String hash) {
    this.rawContent = content;
    this.hash = hash;
    gzippedContent = computeGzippedContent(content);
  }

  private byte[] computeGzippedContent(final String content) {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final OutputStream os = new GZIPOutputStream(new BufferedOutputStream(baos));
      IOUtils.copy(new ByteArrayInputStream(content.getBytes()), os);
      os.close();
      return baos.toByteArray();
    } catch (final IOException e) {
      throw new WroRuntimeException("Problem while computing gzipped content", e).logError();
    }
  };


  /**
   * Factory method.
   *
   * @return {@link ContentHashEntry} based on supplied values.
   */
  public static final ContentHashEntry valueOf(final String rawContent, final String hash) {
    return new ContentHashEntry(rawContent, hash);
  }

  /**
   * @return the content
   */
  public String getRawContent() {
    return this.rawContent;
  }


  /**
   * @param rawContent the content to set
   */
  public void setRawContent(final String rawContent) {
    this.rawContent = rawContent;
  }


  /**
   * @return the hash
   */
  public String getHash() {
    return this.hash;
  }


  /**
   * @param hash the hash to set
   */
  public void setHash(final String hash) {
    this.hash = hash;
  }

  /**
   * @return the gzippedContent
   */
  public byte[] getGzippedContent() {
    return this.gzippedContent;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "hash: " + hash;
  }
}
