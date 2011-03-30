/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestConformColorsCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Aug 15, 2010
 */
public class TestConformColorsCssProcessor {
  private ResourcePostProcessor processor;

  @Before
  public void setUp() {
    processor = new ConformColorsCssProcessor();
  }

  @Test
  public void testColorTransformer()
      throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass())
        + "/conformColors-input.css", "classpath:" + WroUtil.toPackageAsFolder(getClass())
        + "/conformColors-output.css", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
          throws IOException {
        processor.process(reader, writer);
      }
    });
  }
}
