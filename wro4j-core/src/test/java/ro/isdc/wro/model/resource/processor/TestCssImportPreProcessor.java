/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for css import processor.
 *
 * @author Alex Objelean
 */
public class TestCssImportPreProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreFailingProcessor(true);
    Context.set(Context.standaloneContext(), config);
    processor = new CssImportPreProcessor();
    WroTestUtils.initProcessor(processor);
  }

  @Test
  public void testFromFolder()
      throws Exception {
    Context.get().getConfig().setIgnoreMissingResources(false);
    final URL url = getClass().getResource("cssImport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }

  @Test
  public void shouldNotFailWhenInvalidResourceIsFound() throws Exception {
    processInvalidImport();
  }

  @Test(expected = IOException.class)
  public void shouldFailWhenInvalidResourceIsFound() throws Exception {
    Context.get().getConfig().setIgnoreMissingResources(false);
    processInvalidImport();
  }

  private void processInvalidImport()
      throws IOException {
    final Resource resource = Resource.create("someResource.css");
    final Reader reader = new StringReader("@import('/path/to/invalid.css');");
    processor.process(resource, reader, new StringWriter());
  }

  @Test
  public void shouldInvokeImportDetected()
      throws IOException {
    final AtomicInteger times = new AtomicInteger();
    processor = new CssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String foundImportUri) {
        times.incrementAndGet();
      }
    };
    WroTestUtils.initProcessor(processor);
    final Resource resource = Resource.create("someResource.css");
    final Reader reader = new StringReader("@import('/path/to/invalid.css');");
    processor.process(resource, reader, new StringWriter());
    assertEquals(1, times.get());
  }

  /**
   * Fixes <a href="http://code.google.com/p/wro4j/issues/detail?id=505">CssImport processor recursion detection is not
   * thread-safe</a> issue.
   */
  @Test
  public void shouldNotComplainAboutRecursiveImportWhenRunningConcurrently() throws Exception {
    processor = new CssImportPreProcessor() {
      @Override
      protected void onRecursiveImportDetected() {
        throw new WroRuntimeException("Recursion detected");
      }
    };
    WroTestUtils.initProcessor(processor);
    WroTestUtils.runConcurrently(new ContextPropagatingCallable(new Callable<Void>() {
      public Void call()
          throws Exception {
        final Reader reader = new StringReader("@import('/path/to/imported');");
        final Resource resource1 = Resource.create("resource1.css");
        final Resource resource2 = Resource.create("resource2.css");
        if (new Random().nextBoolean()) {
          processor.process(resource1, reader, new StringWriter());
        } else {
          processor.process(resource2, reader, new StringWriter());
        }
        return null;
      }
    }));
  }
}
