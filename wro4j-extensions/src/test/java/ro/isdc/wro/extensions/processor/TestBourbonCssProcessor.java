/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.BourbonCssProcessor;
import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.*;
import java.net.URL;


/**
 * Test ruby sass css processor.
 *
 * @author Simon van der Sluis
 * @created Created on Apr 21, 2010
 */
public class TestBourbonCssProcessor
{

  private static final Logger LOG = LoggerFactory.getLogger(TestBourbonCssProcessor.class);

  /** Location (base) of bourbon sass css with bourbon test resources. */
  private final URL url = getClass().getResource("bourboncss");

  /** A RubySassEngine to test */
  private BourbonCssProcessor bourbonCss;

  @Before
  public void initEngine() {
    bourbonCss = new BourbonCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        LOG.debug("[FAIL] Exception message is: {}", e.getMessage());
        throw e;
      }
    };
  }

  @Test
  public void testFromFolder() throws Exception {
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByName(testFolder, expectedFolder, "scss", "css", bourbonCss);
  }


}
