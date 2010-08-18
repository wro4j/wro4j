/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.util.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which allows to be read multiple times. The only condition is to call reset or close method after reading it.
 *
 * @author Alex Objelean
 * @created 18 Aug 2010
 */
public class UnclosableBufferedInputStream
    extends BufferedInputStream {

  public UnclosableBufferedInputStream(final InputStream in) {
    super(in);
    super.mark(Integer.MAX_VALUE);
  }

  @Override
  public void close()
      throws IOException {
    super.reset();
  }
}