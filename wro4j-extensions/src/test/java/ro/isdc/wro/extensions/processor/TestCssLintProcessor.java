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

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.Transformers;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link CssLintProcessor}.
 *
 * @author Alex Objelean
 */
public class TestCssLintProcessor {
  private ResourceProcessor processor = new CssLintProcessor();


  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("csslint");

    final File testFolder = new File(url.getFile());
    WroTestUtils.compareFromSameFolder(testFolder, new WildcardFileFilter("*.css"), Transformers.noOpTransformer(), processor);
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final CssLintProcessor lessCss = new CssLintProcessor() {
      @Override
      protected void onCssLintException(final CssLintException e, final Resource resource)
        throws Exception {
        throw e;
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          lessCss.process(new StringReader(".label {color: red;}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }


  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
