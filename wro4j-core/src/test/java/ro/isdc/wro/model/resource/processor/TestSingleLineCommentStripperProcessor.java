/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestSingleLineCommentStripperProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestSingleLineCommentStripperProcessor {

  @Test
  public void testFromFolder() throws IOException {
    final ResourcePostProcessor processor = new SingleLineCommentStripperProcessor();

    final URL url = getClass().getResource("singleLine");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      processor);
  }


  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new SingleLineCommentStripperProcessor(), ResourceType.CSS,
        ResourceType.JS);
  }

  // @Test
  // public void testAbsoluteBackgroundUrl()
  // throws IOException {
  // //Output should be the same as input.
  // final String resourcePath = "classpath:ro/isdc/wro/processor/absolutBackgroundUrl.css";
  // compareProcessedResourceContents(resourcePath, resourcePath, new ResourceProcessor() {
  // public void process(final Reader reader, final Writer writer)
  // throws IOException {
  // processor.process(reader, writer);
  // }
  // });
  // }
}
