/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.http.taglib;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Alex Objelean
 */
public abstract class AbstractResourceTag extends TagSupport {
  /** The source path */
  protected String source;

  /**
   * @param source the source to set
   */
  public void setSource(final String source) {
    this.source = source;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
    super.release();
    this.source = null;
  }
}
