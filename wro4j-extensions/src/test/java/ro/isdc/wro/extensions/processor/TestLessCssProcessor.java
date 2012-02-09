/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestLessCssProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(TestLessCssProcessor.class);

  @Test
  public void testFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = new LessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  @Test
  public void executeMultipleTimesDoesntThrowOutOfMemoryException() {
    final LessCss lessCss = new LessCss();
    for (int i = 0; i < 100; i++) {
      lessCss.less("#id {.class {color: red;}}");
    }
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final LessCssProcessor lessCss = new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          lessCss.process(new StringReader("#id {.class {color: red;}}"), new StringWriter());
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
   *
   * @throws Exception
   */
  @Test
  public void shouldFailWhenInvalidLessCssIsProcessed()
      throws Exception {
    final ResourcePreProcessor processor = new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        LOG.debug("[FAIL] Exception message is: {}", e.getMessage());
        throw e;
      };
    };
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
  public void shouldBePossibleToExtendLessCssWithDifferentScriptStream() {
    new LessCss() {
      @Override
      protected InputStream getScriptAsStream() {
        return LessCss.class.getResourceAsStream(LessCss.DEFAULT_LESS_JS);
      }
    }.less("#id {}");
  }
  
  @Test
  public void benchmark() throws Exception {
    int numberOfTests = 10;
    int threadPoolSize = 8;
    final List<Long> noPoolResults = new ArrayList<Long>();
    final List<Long> usePoolResults = new ArrayList<Long>();
    final List<Long> concurrentNoPoolResults = new ArrayList<Long>();
    final List<Long> concurrentUsePoolResults = new ArrayList<Long>();

    //warmUp
//    concurrentBenchmark(5, 8, false);
    
    /*
    //usePool
    for (int i = 0; i < numberOfTests; i++) {
      usePoolResults.add(runBenchmark(new LessCssProcessor().setUsePool(true)));  
    }
    
    //noPool
    for (int i = 0; i < numberOfTests; i++) {
      noPoolResults.add(runBenchmark(new LessCssProcessor().setUsePool(false)));  
    }
    */

    // concurrentNoPool
    concurrentNoPoolResults.add(concurrentBenchmark(numberOfTests, threadPoolSize, false));
    
    //concurrentUsePool
    concurrentUsePoolResults.add(concurrentBenchmark(numberOfTests, threadPoolSize, true));

    LOG.debug("noPool: {}", noPoolResults);
    LOG.debug("usePoolResults: {}", usePoolResults);
    LOG.debug("Without Pool: {}", concurrentNoPoolResults);
    LOG.debug("With    Pool: {}", concurrentUsePoolResults);
  }


  private long concurrentBenchmark(int numberOfTests, int threadPoolSize, boolean usePool)
      throws Exception {
    final LessCssProcessor processor = new LessCssProcessor().setUsePool(usePool);
    StopWatch watch = new StopWatch();
    watch.start("less");
    WroTestUtils.runConcurrently(new Callable<Void>() {
      @Override
      public Void call()
          throws Exception {
        runBenchmark(processor);
        return null;
      }
    }, threadPoolSize, numberOfTests);
    watch.stop();
    return watch.getTotalTimeMillis();
  }


  private long runBenchmark(final LessCssProcessor processor) {
    final URL url = getClass().getResource("lesscss");
    final File testFolder = new File(url.getFile(), "test");
    final StopWatch watch = new StopWatch();
    watch.start("less");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          processor.process(Resource.create(input.getName(), ResourceType.JS), new FileReader(input), new StringWriter());
        } catch (final WroRuntimeException e) {
        }
        return null;
      }
    });      
    watch.stop();
    return watch.getTotalTimeMillis();
  }
}
