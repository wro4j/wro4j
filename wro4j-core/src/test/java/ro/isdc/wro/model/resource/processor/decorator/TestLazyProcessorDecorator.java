package ro.isdc.wro.model.resource.processor.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;

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
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestLazyProcessorDecorator {
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  private ProcessorDecorator mockProcessorDecorator;
  
  private LazyProcessorDecorator victim;
  
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
    mockProcessorDecorator = Mockito.spy(new ProcessorDecorator(new JSMinProcessor()));
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotProcessNullLazyProcessor()
      throws Exception {
    victim = new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return null;
      }
    });
    WroTestUtils.createInjector().inject(victim);
    victim.process(null, mockReader, mockWriter);
  }
  
  @Test
  public void shouldInvokeLazyProcessor()
      throws Exception {
    final ResourceType expectedResourceType = ResourceType.CSS;
    when(mockProcessorDecorator.isMinimize()).thenReturn(true);
    when(mockProcessorDecorator.getSupportedResourceType()).thenReturn(new SupportedResourceType() {
      public Class<? extends Annotation> annotationType() {
        return SupportedResourceType.class;
      }
      
      public ResourceType value() {
        return expectedResourceType;
      }
    });
    when(mockProcessorDecorator.isSupported()).thenReturn(false);
    victim = new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return mockProcessorDecorator;
      }
    });
    WroTestUtils.createInjector().inject(victim);
    victim.process(null, new StringReader(""), new StringWriter());
    assertTrue(victim.isMinimize());
    assertFalse(victim.isSupported());
    assertEquals(expectedResourceType, victim.getSupportedResourceType().value());
    verify(mockProcessorDecorator).process((Resource)Mockito.isNull(), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
}
