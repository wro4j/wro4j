/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test cjson processor.
 *
 * @author Alex Objelean
 */
public class TestCJsonProcessor {

  @Test
  public void testPackFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = new CJsonProcessor(true) {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final URL url = getClass().getResource("cjson");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "pack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void testUnpackFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = new CJsonProcessor(false) {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final URL url = getClass().getResource("cjson");

    final File testFolder = new File(url.getFile(), "pack");
    final File expectedFolder = new File(url.getFile(), "unpack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }


  @Test
  public void shouldBeThreadSafe() throws Exception {
    genericThreadSafeTest(true);
    genericThreadSafeTest(false);
  }

  private void genericThreadSafeTest(boolean pack)
      throws Exception {
    final CJsonProcessor processor = new CJsonProcessor(pack) {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          processor.process(new StringReader("{\"p\" : 1}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task, 20);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new CJsonProcessor(true), ResourceType.JS);
  }
}
