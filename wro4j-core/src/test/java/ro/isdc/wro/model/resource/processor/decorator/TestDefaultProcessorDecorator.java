package ro.isdc.wro.model.resource.processor.decorator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestDefaultProcessorDecorator {
  @Mock
  private MockProcessor mockProcessor;
  private final Resource testResource = Resource.create("/resource.js");
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  
  private DefaultProcessorDecorator victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  private static interface MockProcessor
      extends ResourcePreProcessor {
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    victim = new DefaultProcessorDecorator(mockProcessor, true);
    WroTestUtils.createInjector().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullProcessor() {
    new DefaultProcessorDecorator(null, false);
  }
  
  @Test
  public void shouldReturnOriginalDecoratedProcessor() {
    Assert.assertSame(mockProcessor, victim.getOriginalDecoratedObject());
  }
  
  @Test
  public void shouldPreserveContentWhenProcessingFails()
      throws Exception {
    Context.get().getConfig().setIgnoreFailingProcessor(true);
    
    Mockito.doThrow(new IOException("BOOM")).when(mockProcessor).process(Mockito.any(Resource.class),
        Mockito.any(Reader.class), Mockito.any(Writer.class));
    
    final String resourceContent = "alert(1);";
    final StringWriter writer = new StringWriter();
    final Reader reader = new StringReader(resourceContent);
    
    victim.process(testResource, reader, writer);
    Mockito.verify(mockProcessor).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
    Assert.assertEquals(resourceContent, writer.toString());
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullCriteria() {
    new DefaultProcessorDecorator(mockProcessor, null);
  }
}
