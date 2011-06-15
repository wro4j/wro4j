/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestVariablizeColorsCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Aug 15, 2010
 */
public class TestVariablizeColorsCssProcessor {
  private ResourceProcessor processor;


  @Before
  public void setUp() {
    processor = new VariablizeColorsCssProcessor();
    WroTestUtils.initProcessor(processor);
  }


  @Test
  public void testVariablizeColors()
    throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass())
      + "/variablizeColors-input.css", "classpath:" + WroUtil.toPackageAsFolder(getClass())
      + "/variablizeColors-output.css", processor);
  }
}
