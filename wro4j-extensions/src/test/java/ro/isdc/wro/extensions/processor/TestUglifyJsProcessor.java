/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;
import static ro.isdc.wro.extensions.processor.support.uglify.UglifyJs.Type.UGLIFY;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.support.uglify.UglifyJs;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestUglifyJsProcessor.
 *
 * @author Alex Objelean
 */
public class TestUglifyJsProcessor {
  private File testFolder;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    testFolder = new File(ClassLoader.getSystemResource("test").getFile());
  }

  @Test
  public void shouldUglifyFiles()
      throws IOException {
    final ResourcePostProcessor processor = new UglifyJsProcessor();
    final URL url = getClass().getResource("uglify");

    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void shouldUseReservedNames()
      throws IOException {
    final ResourcePostProcessor processor = new UglifyJsProcessor() {
      @Override
      protected UglifyJs newEngine() {
        return super.newEngine().setReservedNames("name,value");
      }
    };
    final URL url = getClass().getResource("uglify");

    final File testFolder = new File(url.getFile(), "testReservedNames");
    final File expectedFolder = new File(url.getFile(), "expectedReservedNames");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void shouldBeThreadSafe()
      throws Exception {
    final UglifyJsProcessor processor = new UglifyJsProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(new StringReader("alert(1);"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }

  @Test
  public void shouldBePossibleToExtendLessCssWithDifferentScriptStream()
      throws Exception {
    new UglifyJs(UGLIFY) {
      @Override
      protected InputStream getScriptAsStream() {
        return UglifyJs.class.getResourceAsStream(UglifyJs.DEFAULT_UGLIFY_JS);
      }
    }.process("filename", "alert(1);");
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullOptions()
      throws Exception {
    new UglifyJs(UGLIFY) {
      @Override
      protected String createOptionsAsJson()
          throws IOException {
        return null;
      };
    }.process("filename", "alert(1);");
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidJsonOptions()
      throws Exception {
    new UglifyJs(UGLIFY) {
      @Override
      protected String createOptionsAsJson()
          throws IOException {
        return "This is an invalid JSON";
      };
    }.process("filename", "alert(1);");
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new UglifyJsProcessor(), ResourceType.JS);
  }
}
