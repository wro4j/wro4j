/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assume.assumeTrue;
import static ro.isdc.wro.util.WroTestUtils.initProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.css.NodeLessCssProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor based on lessc shell which uses node.
 *
 * @author Alex Objelean
 */
public class TestNodeLessCssProcessor {
  private static boolean isSupported = false;
  @BeforeClass
  public static void beforeClass() {
    //initialize this field only once.
    isSupported = new NodeLessCssProcessor().isSupported();
  }

  /**
   * Checks if the test can be run by inspecting {@link NodeLessCssProcessor#isSupported()}
   */
  @Before
  public void beforeMethod() {
    Context.set(Context.standaloneContext());
    assumeTrue(isSupported);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final ResourceProcessor processor = new NodeLessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedNode");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldWorkProperlyWithUrlRewritingPreProcessor()
      throws Exception {
    final ResourceProcessor processor = ChainedProcessor.create(new CssImportPreProcessor(), new NodeLessCssProcessor());
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedUrlRewriting");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  private static class ChainedProcessor implements ResourceProcessor {
    private List<ResourceProcessor> processors = new ArrayList<ResourceProcessor>();
    private ChainedProcessor(final List<ResourceProcessor> processors) {
      this.processors = processors;
    }
    public static ChainedProcessor create(final ResourceProcessor ... processors) {
      final List<ResourceProcessor> processorsAsList = new ArrayList<ResourceProcessor>();
      if (processors != null) {
        processorsAsList.addAll(Arrays.asList(processors));
      }
      return new ChainedProcessor(processorsAsList);
    }
    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
      Reader tempReader = reader;
      Writer tempWriter = null;
      for (final ResourceProcessor processor : processors) {
        tempWriter = new StringWriter();
        initProcessor(processor);
        processor.process(resource, tempReader, tempWriter);
        tempReader = new StringReader(tempWriter.toString());
      }
      writer.write(tempWriter.toString());
    }
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final NodeLessCssProcessor processor = new NodeLessCssProcessor() {
      @Override
      protected void onException(final Exception e, final String content) {
        throw WroRuntimeException.wrap(e);
      }
    };
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
    final ResourceProcessor processor = new NodeLessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "invalid");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          processor.process(null, new FileReader(input), new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final WroRuntimeException e) {
          //expected to throw exception, continue
        }
        return null;
      }
    });
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new NodeLessCssProcessor(), ResourceType.CSS);
  }
}
