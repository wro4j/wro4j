/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link JsLintProcessor}.
 *
 * @author Alex Objelean
 */
public class TestJsLintProcessor extends AbstractTestLinterProcessor {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor newLinterProcessor() {
    return new JsLintProcessor();
  }


  @Test
  public void testWithOptionsSet()
      throws Exception {
    final ThreadLocal<Throwable> cause = new ThreadLocal<Throwable>();

    final ResourcePostProcessor processor = new JsLintProcessor() {
      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        cause.set(e);
      };
    }.setOptions(new String[] {
      "maxerr=1"
    });

    processor.process(new StringReader("alert(;"), new StringWriter());
    Assert.assertNotNull(cause.get());
  }

  @Test
  public void canBeExecutedMultipleTimes() throws Exception {
    final JsLintProcessor processor = new JsLintProcessor();
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          processor.process(null, new StringReader("var i = 1;"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }
}
