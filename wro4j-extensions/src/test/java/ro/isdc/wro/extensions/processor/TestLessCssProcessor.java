package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.NodeLessCssProcessor;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
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
    Context.set(Context.standaloneContext());
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


  @Test
  public void shouldWorkAsAPreProcessorWithCssImportPreProcessor() throws Exception {
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory();
    managerFactory.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(
        new CssImportPreProcessor()).addPreProcessor(new LessCssProcessor()));
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    final PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
    injector.inject(preProcessorExecutor);

    final List<Resource> resources = new ArrayList<Resource>();
    resources.add(Resource.create("classpath:ro/isdc/wro/extensions/processor/lesscss/test/import.css"));
    final String actual = preProcessorExecutor.processAndMerge(resources, true);
    assertEquals("", actual);
  }
}
