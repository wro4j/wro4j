/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestCommentStripperProcessor.java.
 *
 * @author Ivar Conradi Østhus
 */
public class TestCommentStripperProcessor {
  private final ResourcePostProcessor processor = new CommentStripperProcessor();

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("commentStripper");
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
