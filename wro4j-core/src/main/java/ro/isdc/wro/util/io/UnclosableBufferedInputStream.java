/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.util.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which allows to be read multiple times. The only condition is to call reset or close method after reading it.
 *
 * @author Alex Objelean
 */
public class UnclosableBufferedInputStream
    extends BufferedInputStream {

  public UnclosableBufferedInputStream(final InputStream in) {
    super(in);
    super.mark(Integer.MAX_VALUE);
  }

  public UnclosableBufferedInputStream(final byte[] bytes) {
    this (new ByteArrayInputStream(bytes));
  }

  @Override
  public void close()
      throws IOException {
    super.reset();
  }
}