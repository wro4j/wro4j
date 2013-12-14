/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestCommentStripperProcessor.java.
 *
 * @author Ivar Conradi Østhus
 */
public class TestCommentStripperProcessor {
  private final ResourcePreProcessor processor = new CommentStripperProcessor();
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("commentStripper");
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS, ResourceType.JS);
  }
}
