/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Test CoffeeScript processor.
 *
 * @author Alex Objelean
 * @since 1.3.6
 * @created Created on Mar 26, 2011
 */
public class TestCoffeeScriptProcessor {
  private ResourcePostProcessor processor;


  @Before
  public void setUp() {
    processor = new CoffeeScriptProcessor();
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
    processor = new CoffeeScriptProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        counter.increment();
      }
    };

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "coffee",
      processor);
    Assert.assertEquals(2, counter.getIndex());
  }


  @Test
  public void testSimple()
    throws IOException {
    final URL url = getClass().getResource("coffeeScript/simple");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "coffee", processor);
  }


  @Test
  public void testAdvanced()
    throws IOException {
    final URL url = getClass().getResource("coffeeScript/advanced");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "coffee", processor);
  }
}
