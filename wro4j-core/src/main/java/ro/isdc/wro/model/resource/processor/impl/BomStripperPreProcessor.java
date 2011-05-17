/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Removes BOM (Byte Order Mark) characters from the beginning of merged resource files.
 * <p>
 *
 * @see http://en.wikipedia.org/wiki/Byte_order_mark.
 * @author Alex Objelean
 * @created Created on Feb 20, 2010
 */
public final class BomStripperPreProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * A stream which removes BOM characters.
   */
  private static class BomStripperInputStream
      extends PushbackInputStream {
    private static final int[][] BOMS = { { 0x00, 0x00, 0xFE, 0xFF },
      { 0xFF, 0xFE, 0x00, 0x00 },
      { 0x2B, 0x2F, 0x76, 0x38 },
      { 0x2B, 0x2F, 0x76, 0x39 },
      { 0x2B, 0x2F, 0x76, 0x2B },
      { 0x2B, 0x2F, 0x76, 0x2F },
      { 0xDD, 0x73, 0x66, 0x73 },
      { 0xEF, 0xBB, 0xBF },
      { 0x0E, 0xFE, 0xFF },
      { 0xFB, 0xEE, 0x28 },
      { 0xFE, 0xFF },
      { 0xFF, 0xFE } };


    /**
     * Removes a BOM characters from chained inputStream.
     */
    public BomStripperInputStream(final InputStream is) throws IOException {
      super(is, 4);
      final int[] bytes = { read(), read(), read(), read() };
      int count = 0;
      for (final int[] bom : BOMS) {
        count = testForBOM(bom, bytes);
        if (count != 0) {
          break;
        }
      }
      for (int index = bytes.length - 1; index >= count; index--) {
        if (bytes[index] != -1) {
          unread(bytes[index]);
        }
      }
    }

    private int testForBOM(final int[] bom, final int[] bytes) {
      for (int index = 0; index < bom.length; index++) {
        if (bom[index] != bytes[index]) {
          return 0;
        }
      }
      return bom.length;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    try {
      System.out.println("BOM:");
      // using ReaderInputStream instead of ByteArrayInputStream, cause processing to freeze
      final InputStream is = new BomStripperInputStream(new ByteArrayInputStream(IOUtils.toByteArray(reader)));
      IOUtils.copy(is, writer, Context.get().getConfig().getEncoding());
      System.out.println("END BOM");
    } finally {
      reader.close();
      writer.close();
    }
  }
}
