/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestYUIJsCompressorProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = new YUIJsCompressorProcessor();

  @Test
  public void testWicketEventJsWithMunge()
    throws IOException {
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/wicket-event.js", "classpath:"
      + WroUtil.toPackageAsFolder(getClass()) + "/wicket-event.yui-munge.js", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    });
  }

  @Test
  public void testWicketEventJsWithNoMunge()
    throws IOException {
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/wicket-event.js", "classpath:"
      + WroUtil.toPackageAsFolder(getClass()) + "/wicket-event.yui-nomunge.js", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        new YUIJsCompressorProcessor(false).process(reader, writer);
      }
    });
  }

  @Test
  public void testWithMunge()
    throws IOException {
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/input.js", "classpath:"
      + WroUtil.toPackageAsFolder(getClass()) + "/yuijscompressor-munge-output.js", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    });
  }



  @Test
  public void testNoMunge()
    throws IOException {
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/input.js", "classpath:"
      + WroUtil.toPackageAsFolder(getClass()) + "/yuijscompressor-nomunge-output.js", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        new YUIJsCompressorProcessor(false).process(reader, writer);
      }
    });
  }
}
