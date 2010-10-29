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
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestLessCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestPackerProcessor extends AbstractWroTest {
  private ResourcePostProcessor processor;

  @Before
  public void setUp() {
    processor = new PackerJsProcessor();
  }

  @Test
  public void testFromFolder() throws IOException {
    final URL url = getClass().getResource("packer");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "pack.js", WroUtil.newResourceProcessor(processor));
  }
}
