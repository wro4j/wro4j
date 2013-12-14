package ro.isdc.wro.model.resource.processor.decorator;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.Writer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestBenchmarkProcessorDecorator {
  @Mock
  private Resource mockResource;
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  @Mock
  private ResourcePreProcessor mockProcessor;
  @Mock
  private Runnable mockBefore;
  @Mock
  private Runnable mockAfter;
  private BenchmarkProcessorDecorator victim;
  
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
    victim = new BenchmarkProcessorDecorator(mockProcessor) {
      @Override
      void before(final StopWatch stopWatch) {
        super.before(stopWatch);
        mockBefore.run();
      }
      
      @Override
      void after(final StopWatch stopWatch) {
        super.after(stopWatch);
        mockAfter.run();
      }
    };
    WroTestUtils.createInjector().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test
  public void shouldInvokeBeforeAndAfterInDebugMode()
      throws Exception {
    Context.get().getConfig().setDebug(true);
    victim.process(mockResource, mockReader, mockWriter);
    Mockito.verify(mockBefore).run();
    Mockito.verify(mockAfter).run();
  }
  
  @Test
  public void shouldNotInvokeBeforeAndAfterInProductionMode()
      throws Exception {
    Context.get().getConfig().setDebug(false);
    victim.process(mockResource, mockReader, mockWriter);
    Mockito.verify(mockBefore, Mockito.never()).run();
    Mockito.verify(mockAfter, Mockito.never()).run();
  }
}
