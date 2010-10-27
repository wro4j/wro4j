/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;
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
  //uri path of the test resources
  private static final String URI_PATH_COMMON = "classpath:" + PATH_COMMON;
  private ResourcePostProcessor processor;


  @Before
  public void setUp() {
    processor = new SassCssProcessor();
  }


  /**
   * This test is ignored, because its strangely fails when building with maven, but works when running in IDE.
   */
  @Test
  public void testLessCssFromFolder()
    throws IOException {

    final String fullPath = FilenameUtils.getFullPath(PATH_COMMON);
    final URL url = ClassLoader.getSystemResource(fullPath);
    final File sassFolder = new File(url.getFile());
    final Collection<File> lessFiles = FileUtils.listFiles(sassFolder, new WildcardFileFilter("*.sass"),
      FalseFileFilter.INSTANCE);

    final ResourceProcessor resourceProcessor = new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    };

    for (final File file : lessFiles) {
      final InputStream lessFileStream = new FileInputStream(file);
      final InputStream cssFileStream = new FileInputStream(new File(sassFolder, FilenameUtils.getBaseName(file.getName()) + ".css"));
      LOG.debug("processing: " + file.getName());
      compareProcessedResourceContents(lessFileStream, cssFileStream, resourceProcessor);
    }
  }
}
