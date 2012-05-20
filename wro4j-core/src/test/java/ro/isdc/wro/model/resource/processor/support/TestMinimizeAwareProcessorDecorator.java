package ro.isdc.wro.model.resource.processor.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.model.group.processor.MinimizeAwareProcessorDecorator;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.MinimizeAware;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * @author Alex Objelean
 */
public class TestMinimizeAwareProcessorDecorator {
  @Mock
  private ResourcePreProcessor mockPreProcessor;
  @Mock
  private ResourcePostProcessor mockPostProcessor;
  private Reader mockReader;
  private Writer mockWriter;
  private MinimizeAwareProcessorDecorator victim;
  
  private static class MinimizeAwarePostProcessor
      implements ResourcePostProcessor, MinimizeAware {
    public void process(final Reader reader, final Writer writer)
        throws IOException {
      // do nothing
    }
    
    public boolean isMinimize() {
      return true;
    }
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockReader = new StringReader("");
    mockWriter = new StringWriter();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullProcessor() {
    victim = new MinimizeAwareProcessorDecorator(null, true);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptObjectWhichIsNotProcessor() {
    victim = new MinimizeAwareProcessorDecorator(new Object(), true);
  }
  
  @Test
  public void shouldInvokeMinimizePreProcessorWhenMinimizeIsRequired()
      throws Exception {
    ResourcePreProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, true);
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.atLeastOnce()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldNotInvokeMinimizePreProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    ResourcePreProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, false);
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.never()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldInvokeMinimizePostProcessorWhenMinimizeIsRequired()
      throws Exception {
    
    ResourcePostProcessor processor = Mockito.spy(new MinimizeAwarePostProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, true);
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.atLeastOnce()).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldNotInvokeMinimizePostProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    ResourcePostProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, false);
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.never()).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldInvokePreProcessorWhenMinimizeIsRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor, true);
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process(Mockito.any(Resource.class),
        Mockito.any(Reader.class), Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldInvokePreProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor, false);
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process(Mockito.any(Resource.class),
        Mockito.any(Reader.class), Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldInvokePostProcessorWhenMinimizeIsRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPostProcessor, true);
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPostProcessor, Mockito.atLeastOnce()).process(Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
  
  @Test
  public void shouldInvokePostProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPostProcessor, false);
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPostProcessor, Mockito.atLeastOnce()).process(Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }
}
