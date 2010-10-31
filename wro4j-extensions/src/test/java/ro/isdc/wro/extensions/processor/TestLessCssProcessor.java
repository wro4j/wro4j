/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test less css processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestLessCssProcessor extends AbstractWroTest {
  private ResourcePostProcessor processor;


  @Before
  public void setUp() {
    processor = new LessCssProcessor();
  }

  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("lesscss");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "less", "css", WroUtil.newResourceProcessor(processor));
  }
}
