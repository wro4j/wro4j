/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestLessCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestSassCssProcessor extends AbstractWroTest {
  private static final Logger LOG = LoggerFactory.getLogger(TestSassCssProcessor.class);
  //Path the the folder where test resources are located
  private static final String PATH_COMMON = WroUtil.toPackageAsFolder(TestSassCssProcessor.class) + "/sasscss/";
  private ResourcePostProcessor processor;


  @Before
  public void setUp() {
    processor = new SassCssProcessor();
  }

  @Test
  public void testSassCssFromFolder()
    throws IOException {
    final String fullPath = FilenameUtils.getFullPath(PATH_COMMON);
    final URL url = ClassLoader.getSystemResource(fullPath);
    final File sassFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sassFolder, "sass", "css", WroUtil.newResourceProcessor(processor));
  }
}
