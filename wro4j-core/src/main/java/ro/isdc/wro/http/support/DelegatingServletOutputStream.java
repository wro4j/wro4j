/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http.support;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * Delegating implementation of {@link javax.servlet.ServletOutputStream}.
 *
 * @author Alex Objelean
 * @created Created on Nov 14, 2008
 */
public class DelegatingServletOutputStream extends ServletOutputStream {
  /**
   * Target {@link OutputStream} object.
   */
  private final OutputStream targetStream;

  /**
   * Create a DelegatingServletOutputStream for the given target stream.
   *
   * @param targetStream
   *          the target stream (never <code>null</code>)
   */
  public DelegatingServletOutputStream(final OutputStream targetStream) {
    if (targetStream == null) {
      throw new IllegalArgumentException("Target OutputStream must not be null");
    }
    this.targetStream = targetStream;
  }

  /**
   * Return the underlying target stream (never <code>null</code>).
   */
  public final OutputStream getTargetStream() {
    return this.targetStream;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final int b) throws IOException {
    this.targetStream.write(b);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    super.flush();
    this.targetStream.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    super.close();
    this.targetStream.close();
  }
}
