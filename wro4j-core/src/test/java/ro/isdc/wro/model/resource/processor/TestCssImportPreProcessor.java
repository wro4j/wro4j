/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
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
  private static final Random RANDOM = new Random();
  private ResourcePreProcessor victim;

  @Before
  public void setUp() {
    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreFailingProcessor(true);
    Context.set(Context.standaloneContext(), config);
    victim = new CssImportPreProcessor();
    WroTestUtils.initProcessor(victim);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    Context.get().getConfig().setIgnoreMissingResources(false);
    final URL url = getClass().getResource("cssImport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", victim);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(victim, ResourceType.CSS);
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
    victim.process(resource, reader, new StringWriter());
  }

  @Test
  public void shouldInvokeImportDetected()
      throws IOException {
    final AtomicInteger times = new AtomicInteger();
    victim = new CssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String foundImportUri) {
        times.incrementAndGet();
      }
    };
    WroTestUtils.initProcessor(victim);
    final Resource resource = Resource.create("someResource.css");
    final Reader reader = new StringReader("@import('/path/to/invalid.css');");
    victim.process(resource, reader, new StringWriter());
    assertEquals(1, times.get());
  }

  @Test
  public void shouldCorrectlyDetectImportWithDoubleQuotes()
      throws Exception {
    assertImportDetected("@import(\"child/style.css\");", "/css/child/style.css");
  }

  @Test
  public void shouldCorrectlyDetectImport()
      throws Exception {
    assertImportDetected("@import(child/style.css);", "/css/child/style.css");
  }

  @Test
  public void shouldCorrectlyDetectImportWithSimpleQuotes()
      throws Exception {
    assertImportDetected("@import('child/style.css');", "/css/child/style.css");
  }

  void assertImportDetected(final String content, final String expected)
      throws IOException {
    final AtomicBoolean found = new AtomicBoolean();
    victim = new CssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String foundImportUri) {
        assertEquals(expected, foundImportUri);
        found.set(true);
      }
      @Override
      protected String doTransform(final String cssContent, final List<Resource> foundImports)
          throws IOException {
        return "";
      }
    };
    WroTestUtils.initProcessor(victim);
    victim.process(Resource.create("/css/parent.css"), new StringReader(content), new StringWriter());
    assertTrue(found.get());
  }

  /**
   * Fixes <a href="http://code.google.com/p/wro4j/issues/detail?id=505">CssImport processor recursion detection is not
   * thread-safe</a> issue.
   */
  @Test
  public void shouldNotComplainAboutRecursiveImportWhenRunningConcurrently() throws Exception {
    final AtomicReference<Map<?, ?>> contextMapRef = new AtomicReference<Map<?,?>>();
    victim = new CssImportPreProcessor() {
      {{
        contextMapRef.set(getContextMap());
      }}
      @Override
      protected void onRecursiveImportDetected() {
        throw new WroRuntimeException("Recursion detected");
      }
    };
    WroTestUtils.initProcessor(victim);
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        Context.set(Context.standaloneContext());
        final Reader reader = new StringReader("@import('/path/to/imported');");
        final Resource resource1 = Resource.create("resource1.css");
        final Resource resource2 = Resource.create("resource2.css");
        if (RANDOM.nextBoolean()) {
          victim.process(resource1, reader, new StringWriter());
        } else {
          victim.process(resource2, reader, new StringWriter());
        }
        Context.unset();
        return null;
      }
    }));
    assertTrue(contextMapRef.get().isEmpty());
  }
}
