/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * TestLessCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestLessCssProcessor extends AbstractWroTest {
  private static final Logger LOG = LoggerFactory.getLogger(TestLessCssProcessor.class);
  private ResourcePostProcessor processor;

  @Before
  public void setUp() {
    processor = new LessCssProcessor();
  }

  @Test
  public void testMixins()
    throws IOException {
    LOG.debug("testMixins");
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/lesscss/mixins.css",
      "classpath:ro/isdc/wro/extensions/processor/lesscss/mixins-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }


  @Test
  public void testNestedRules()
    throws IOException {
    LOG.debug("testNestedRules");
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/lesscss/nestedRules.css",
      "classpath:ro/isdc/wro/extensions/processor/lesscss/nestedRules-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }


  @Test
  public void testOperations()
    throws IOException {
    LOG.debug("testOperations");
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/lesscss/operations.css",
      "classpath:ro/isdc/wro/extensions/processor/lesscss/operations-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }

  @Test
  public void testVariables()
    throws IOException {
    LOG.debug("testVariables");
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/lesscss/variables.css",
      "classpath:ro/isdc/wro/extensions/processor/lesscss/variables-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }

  /**
   * This test is ignored, because its strangely fails when building with maven, but works when running in IDE.
   */
  @Test
  public void testInvalid()
    throws IOException {
    LOG.debug("testInvalid");
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/lesscss/invalid.css",
      "classpath:ro/isdc/wro/extensions/processor/lesscss/invalid-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }
}
