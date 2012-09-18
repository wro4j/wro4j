package ro.isdc.wro.extensions.processor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.NodeLessCssProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;

/**
 * @author Alex Objelean
 */
public class TestLessCssProcessor {
  @Mock
  private Resource mockResource;
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  @Mock
  private NodeLessCssProcessor mockNodeProcessor;
  @Mock
  private ResourcePreProcessor mockRhinoProcessor;
  private ResourcePreProcessor victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    //use lazy initialization to defer constructor invocation
    victim = new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new LessCssProcessor() {
          @Override
          protected ResourcePreProcessor createRhinoProcessor() {
            return mockRhinoProcessor;
          }
          
          @Override
          protected NodeLessCssProcessor createNodeProcessor() {
            return mockNodeProcessor;
          }
        };
      }
    });
  }
  
  @Test
  public void shouldUseNodeProcessorWhenSupported() throws Exception {
    when(mockNodeProcessor.isSupported()).thenReturn(true);
    victim.process(mockResource, mockReader, mockWriter);
    verify(mockNodeProcessor, Mockito.times(1)).process(mockResource, mockReader, mockWriter);
    verify(mockRhinoProcessor, Mockito.never()).process(mockResource, mockReader, mockWriter);
  }
  
  @Test
  public void shouldUseFallbackProcessorWhenNodeNotSupported() throws Exception {
    when(mockNodeProcessor.isSupported()).thenReturn(false);
    victim.process(mockResource, mockReader, mockWriter);
    verify(mockNodeProcessor, Mockito.never()).process(mockResource, mockReader, mockWriter);
    verify(mockRhinoProcessor, Mockito.times(1)).process(mockResource, mockReader, mockWriter);
  }
}
