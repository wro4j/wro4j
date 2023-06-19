/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link JsHintProcessor}.
 *
 * @author Alex Objelean
 */
public class TestJsHintProcessor
    extends AbstractTestLinterProcessor {
  private JsHintProcessor victim;

  @Before
  public void setUp() {
    victim = new JsHintProcessor();
  }

  @Override
  protected ResourcePreProcessor newLinterProcessor() {
    return new JsHintProcessor();
  }

  @Test
  public void testWithOptionsSet()
      throws Exception {
    final ThreadLocal<Throwable> cause = new ThreadLocal<Throwable>();

    final ResourcePostProcessor processor = new JsHintProcessor() {
      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        cause.set(e);
      };
    }.setOptionsAsString("maxerr=1");

    processor.process(new StringReader("alert(;"), new StringWriter());
    Assert.assertNotNull(cause.get());
  }

  /**
   * This test was created initially to prove that {@link JsHintProcessor} is thread-safe, but it doesn't work well when
   * trying to reuse the scope. TODO: This needs to be investigated.
   */
  @Test
  public void canBeExecutedMultipleTimes()
      throws Exception {
    final JsHintProcessor processor = new JsHintProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(new StringReader("alert(1);"), new StringWriter());
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
    WroTestUtils.assertProcessorSupportResourceTypes(new JsHintProcessor(), ResourceType.JS);
  }

  @Test
  public void canSetNullOptions()
      throws Exception {
    victim.setOptionsAsString("");
    victim.process(null, new StringReader("alert(1);"), new StringWriter());
  }

  @Test(expected = LinterException.class)
  public void shouldOverrideDefaultOptions()
      throws Exception {
    victim = new JsHintProcessor() {
      @Override
      protected String createDefaultOptions() {
        return "bitwise";
      }

      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        throw e;
      }
    };
    victim.process(null, new StringReader("true & false"), new StringWriter());
  }
}
