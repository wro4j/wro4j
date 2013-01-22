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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.RhinoTypeScriptProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor based on lessc shell which uses node.
 *
 * @author Alex Objelean
 */
public class TestRhinoTypeScriptProcessor {
  private ResourcePreProcessor victim;
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new RhinoTypeScriptProcessor();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("typescript");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedRhino");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", victim);
  }


  @Test
  public void shouldBeThreadSafe() throws Exception {
    final ResourcePreProcessor processor = new RhinoTypeScriptProcessor() {
      @Override
      protected void onException(final Exception e, final String content) {
        throw WroRuntimeException.wrap(e);
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(null, new StringReader("class Animal {}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task, 7);
  }

  /**
   * Test that processing invalid less css produces exceptions
   */
  @Test
  public void shouldFailWhenInvalidTypeScriptIsProcessed()
      throws Exception {
    final URL url = getClass().getResource("typescript");
    final File testFolder = new File(url.getFile(), "invalid");
    final ResourcePreProcessor decorated = new ExceptionHandlingProcessorDecorator(victim);
    WroTestUtils.createInjector().inject(decorated);
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          decorated.process(Resource.create(input.getPath(), ResourceType.JS), new FileReader(input), new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final WroRuntimeException e) {
          // expected to throw exception, continue
        }
        return null;
      }
    });
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new RhinoTypeScriptProcessor(), ResourceType.JS);
  }
}
