/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


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
      protected void onLinterException(final LinterException e, final Resource resource) throws Exception {
        cause.set(e);
      };
    }.setOptions(new String[] {
      "maxerr=1"
    });

    processor.process(new StringReader("alert(;"), new StringWriter());
    Assert.assertNotNull(cause.get());
  }
}
