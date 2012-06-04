/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Test CoffeeScript processor.
 *
 * @author Alex Objelean
 * @since 1.3.6
 * @created Created on Mar 26, 2011
 */
public class TestCoffeeScriptProcessor {
  private ResourceProcessor processor;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    processor = new CoffeeScriptProcessor();
  }


  @After
  public void tearDown() {
    Context.unset();
  }

  private static class Counter {
    private int index;


    public void increment() {
      index++;
    }


    public int getIndex() {
      return this.index;
    }
  }


  /**
   * Test that by default, failing to process a js with coffeeScript, will leave the result unchanged.
   */
  @Test
  public void testExceptions()
    throws IOException {
    final URL url = getClass().getResource("coffeeScript/exceptions");
    final Counter counter = new Counter();
    processor = new ExceptionHandlingProcessorDecorator(new CoffeeScriptProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        counter.increment();
        throw e;
      }
    }) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      }
    };

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      processor);
    Assert.assertEquals(2, counter.getIndex());
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final CoffeeScriptProcessor processor = new CoffeeScriptProcessor();
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          processor.process(null, new StringReader("square = (x) -> x * x"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task, 30);
  }

  @Test
  public void testSimple()
    throws IOException {
    final URL url = getClass().getResource("coffeeScript/simple");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }


  @Test
  public void testAdvanced()
    throws IOException {
    final URL url = getClass().getResource("coffeeScript/advanced");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
