/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.LessCssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.support.ChainedProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor based on lessc shell which uses node.
 *
 * @author Alex Objelean
 */
public class TestLess4jProcessor {
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final ResourcePreProcessor processor = new Less4jProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedLess4j");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  @Test
  public void shouldBeThreadSafe() throws Exception {
    final ResourcePreProcessor processor = new Less4jProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(null, new StringReader("#id {.class {color: red;}}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }

  /**
   * Test that processing invalid less css produces exceptions
   */
  @Test
  public void shouldFailWhenInvalidLessCssIsProcessed()
      throws Exception {
    final ResourcePreProcessor processor = new Less4jProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "invalid");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          processor.process(Resource.create(input.getPath(), ResourceType.CSS), new FileReader(input),
              new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final Exception e) {
          //expected to throw exception, continue
        }
        return null;
      }
    });
  }


  @Test
  public void shouldDetectProperlyCssImportStatements()
      throws Exception {
    final ResourcePreProcessor processor = ChainedProcessor.create(new LessCssImportPreProcessor(), new Less4jProcessor());
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedLessCssImport");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new Less4jProcessor(), ResourceType.CSS);
  }
}
