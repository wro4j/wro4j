package ro.isdc.wro.extensions.processor.js;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.concurrent.Callable;

import org.apache.commons.io.output.NullWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestAbstractNodeWithFallbackProcessor {
  @Mock
  private ResourcePreProcessor nodeProcessor;
  @Mock
  private ResourcePreProcessor fallbackProcessor;
  private AbstractNodeWithFallbackProcessor victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new AbstractNodeWithFallbackProcessor() {
      @Override
      protected ResourcePreProcessor createNodeProcessor() {
        return nodeProcessor;
      }

      @Override
      protected ResourcePreProcessor createFallbackProcessor() {
        return fallbackProcessor;
      }
    };
    WroTestUtils.createInjector().inject(victim);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldNotFailWhenProcessorIsCreatedConcurrently()
      throws Exception {
    WroTestUtils.runConcurrently(ContextPropagatingCallable.decorate(new Callable<Void>() {
      @Override
      public Void call()
          throws Exception {
        victim.process(new StringReader(""), new NullWriter());
        return null;
      }
    }));
  }
}
