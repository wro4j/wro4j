/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.rhino.packer.PackerJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * TestLessCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestPackerProcessor extends AbstractWroTest {
  private static final Logger LOG = LoggerFactory.getLogger(TestPackerProcessor.class);
  private ResourcePostProcessor processor;

  @Before
  public void setUp() {
    processor = new PackerJsProcessor();
  }

  @Test
  public void testPacker()
    throws IOException {
    LOG.debug("testPacker");
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/packer-input.js",
      "classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/packer-output.js", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
          System.out.println("Process ready");
          System.in.read();
        }
      });
  }
}
