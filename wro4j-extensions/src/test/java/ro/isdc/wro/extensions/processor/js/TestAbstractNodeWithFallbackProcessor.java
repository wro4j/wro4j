package ro.isdc.wro.extensions.processor.js;

import java.io.StringReader;
import java.util.concurrent.Callable;

import org.apache.commons.io.output.NullWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestAbstractNodeWithFallbackProcessor {
  @Mock
  private ResourceProcessor nodeProcessor;
  @Mock
  private ResourceProcessor fallbackProcessor;
  private AbstractNodeWithFallbackProcessor victim;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new AbstractNodeWithFallbackProcessor() {
      @Override
      protected ResourceProcessor createNodeProcessor() {
        return nodeProcessor;
      }

      @Override
      protected ResourceProcessor createFallbackProcessor() {
        return fallbackProcessor;
      }
    };
    WroTestUtils.createInjector().inject(victim);
  }

  @Test
  public void shouldNotFailWhenProcessorIsCreatedConcurrently()
      throws Exception {
    WroTestUtils.runConcurrently(ContextPropagatingCallable.decorate(new Callable<Void>() {
      @Override
      public Void call()
          throws Exception {
        victim.process(null, new StringReader(""), new NullWriter());
        return null;
      }
    }));
  }

  @After
  public void tearDown() {
    Context.unset();
  }

}
