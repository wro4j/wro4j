package ro.isdc.wro.util.io;

import java.io.OutputStream;


/**
 * Implementation of {@link OutputStream} that simply discards written bytes.
 *
 * @author Alex Objelean
 * @since 1.6.4
 */
public final class NullOutputStream
    extends OutputStream {
  /**
   * Discards the specified int.
   */
  @Override
  public void write(final int b) {
  }

  /**
   * Discards the specified byte array.
   */
  @Override
  public void write(final byte[] b, final int off, final int len) {
  }
}
