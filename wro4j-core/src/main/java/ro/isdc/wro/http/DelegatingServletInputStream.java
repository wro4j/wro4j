/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * Delegating implementation of {@link javax.servlet.ServletInputStream}.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 14, 2008
 */
public final class DelegatingServletInputStream extends ServletInputStream {
  /**
   * Source {@link InputStream} object.
   */
  private final InputStream sourceStream;

  /**
   * Create a DelegatingServletInputStream for the given source stream.
   * 
   * @param sourceStream
   *          the target stream (never <code>null</code>)
   */
  public DelegatingServletInputStream(final InputStream sourceStream) {
    if (sourceStream == null) {
      throw new IllegalArgumentException("Source InputStream must not be null");
    }
    this.sourceStream = sourceStream;
  }

  /**
   * Return the underlying source stream (never <code>null</code>).
   */
  public final InputStream getSourceStream() {
    return this.sourceStream;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read() throws IOException {
    return this.sourceStream.read();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    super.close();
    this.sourceStream.close();
  }
}
