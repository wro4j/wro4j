/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.cache;

import java.io.Serializable;


/**
 * Entry holding a resource content along with its associated hash.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public final class ContentHashEntry
  implements Serializable {
  private String content;
  private String hash;

  private ContentHashEntry(final String content, final String hash) {
    this.content = content;
    this.hash = hash;
  };


  /**
   * Factory method.
   *
   * @return {@link ContentHashEntry} based on supplied values.
   */
  public static final ContentHashEntry valueOf(final String content, final String hash) {
    return new ContentHashEntry(content, hash);
  }

  /**
   * @return the content
   */
  public String getContent() {
    return this.content;
  }


  /**
   * @param content the content to set
   */
  public void setContent(final String content) {
    this.content = content;
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
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "hash: " + hash;
  }
}
