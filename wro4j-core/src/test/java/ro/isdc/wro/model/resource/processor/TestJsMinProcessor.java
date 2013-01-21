/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestJsMinProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestJsMinProcessor {
  private ResourcePreProcessor processor;
  @Before
  public void setUp() {
    processor = new JSMinProcessor();
    Context.set(Context.standaloneContext());
    WroTestUtils.createInjector().inject(processor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("jsmin");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }


  /**
   * Tests a known issue with JsMin.
   */
  @Test(expected = WroRuntimeException.class)
  public void shoudlFailWhenCompilingAnExpressionWithNewLines()
      throws Exception {
    processor.process(null, new StringReader("Math.round(4\n/3);"), new StringWriter());

  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.JS);
  }
}
