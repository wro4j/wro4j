package ro.isdc.wro.model.resource.processor.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * @author Alex Objelean
 */
public class TestSupportAwareProcessorDecorator {
  @Mock
  private MockProcessor mockProcessor;
  @Mock
  private Resource mockResource;
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  
  private SupportAwareProcessorDecorator victim;
  
  private static interface MockProcessor
      extends ResourcePreProcessor, SupportAware {
  }
  
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
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    victim = new SupportAwareProcessorDecorator(mockProcessor);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullProcessor() {
    new SupportAwareProcessorDecorator(null);
  }
  
  @Test
  public void shouldCorrectlyDetectSupportOfDeepNestedProcessor() {
    victim = new SupportAwareProcessorDecorator(new ProcessorDecorator(new ProcessorDecorator(new JSMinProcessor())));
    assertTrue(victim.isSupported());
  }
  
  @Test
  public void shouldInvokeDecoratedProcessorWhenProcessorIsSupported()
      throws Exception {
    Mockito.when(mockProcessor.isSupported()).thenReturn(true);
    victim = new SupportAwareProcessorDecorator(mockProcessor);
    
    victim.process(mockResource, mockReader, mockWriter);
    Mockito.verify(mockProcessor).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
  
  @Test(expected = WroRuntimeException.class)
  public void shouldThrowExceptionWhenProcessorIsNotSupported()
      throws Exception {
    Mockito.when(mockProcessor.isSupported()).thenReturn(false);
    victim = new SupportAwareProcessorDecorator(mockProcessor);
    
    victim.process(mockResource, mockReader, mockWriter);
  }
}
