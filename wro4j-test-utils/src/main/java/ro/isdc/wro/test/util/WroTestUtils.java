/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.test.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;

/**
 * AbstractWroTest.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 28, 2008
 */
public class WroTestUtils {
  /**
   * Compare contents of two resources (files) by performing some sort of
   * processing on input resource.
   * 
   * @param inputResourceUri
   *          uri of the resource to process.
   * @param expectedContentResourceUri
   *          uri of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  public static void compareProcessedResourceContents(
      final Reader resultReader, final Reader expectedReader,
      final ResourceProcessor processor) throws IOException {
    final Writer resultWriter = new StringWriter();
    processor.process(resultReader, resultWriter);
    final Writer expectedWriter = new StringWriter();
    IOUtils.copy(expectedReader, expectedWriter);
    final String expected = replaceTabsWithSpaces(expectedWriter.toString()
        .trim());
    final String actual = replaceTabsWithSpaces(resultWriter.toString().trim());
    Assert.assertEquals(expected, actual);
  }

  /**
   * Replace tabs with spaces.
   * 
   * @param input
   *          from where to remove tabs.
   * @return cleaned string.
   */
  private static final String replaceTabsWithSpaces(final String input) {
    // replace tabs with spaces
    return input.replaceAll("\\t", "  ");
  }
}
