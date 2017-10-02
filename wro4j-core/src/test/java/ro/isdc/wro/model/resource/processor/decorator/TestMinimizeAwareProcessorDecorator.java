package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
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
  /**
   * A processor which performs some sort of minimization.
   */
  private static class MinimizeAwareProcessor
      implements ResourcePreProcessor, ResourcePostProcessor, MinimizeAware {
    public void process(final Reader reader, final Writer writer)
        throws IOException {
    }
    public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
    }
    public boolean isMinimize() {
      return true;
    }
  }

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    mockReader = new StringReader("");
    mockWriter = new StringWriter();
  }

  private void initVictim() {
    InjectorBuilder.create(new BaseWroManagerFactory()).build().inject(victim);
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
    final ResourcePreProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, true);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.atLeastOnce()).process((Resource)Mockito.isNull(), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldNotInvokeMinimizePreProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    final ResourcePreProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, false);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.never()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokeMinimizePostProcessorWhenMinimizeIsRequired()
      throws Exception {

    final MinimizeAwareProcessor processor = Mockito.spy(new MinimizeAwareProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, true);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.atLeastOnce()).process((Resource)Mockito.isNull(), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldNotInvokeMinimizePostProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    final ResourcePostProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, false);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(processor, Mockito.never()).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokePreProcessorWhenMinimizeIsRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor, true);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process((Resource)Mockito.isNull(),
        Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokePreProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor, false);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process((Resource)Mockito.isNull(),
        Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokePostProcessorWhenMinimizeIsRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPostProcessor, true);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPostProcessor, Mockito.atLeastOnce()).process(Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokePostProcessorWhenMinimizeIsNotRequired()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPostProcessor, false);
    initVictim();
    victim.process(mockReader, mockWriter);
    Mockito.verify(mockPostProcessor, Mockito.atLeastOnce()).process(Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldLeaveContentUnchangedWhenProcessorIsSkipped()
      throws Exception {
    final ResourcePreProcessor processor = Mockito.spy(new JSMinProcessor());
    victim = new MinimizeAwareProcessorDecorator(processor, false);
    initVictim();
    final String resourceContent = "var i      =     1;";
    final StringWriter writer = new StringWriter();
    victim.process(new StringReader(resourceContent), writer);
    Mockito.verify(mockPostProcessor, Mockito.never()).process(Mockito.any(Reader.class),
        Mockito.any(Writer.class));
    Assert.assertEquals(resourceContent, writer.toString());
  }

  @Test
  public void shouldInvokePreProcessor()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor);
    initVictim();
    victim.process(null, mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process((Resource)Mockito.isNull(), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }


  @Test
  public void shouldInvokePreProcessorWithResourceWantingMinimize()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor);
    initVictim();
    final Resource resource = Resource.create("someResource.js");
    resource.setMinimize(true);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokePreProcessorWithResourceNotWantingMinimize()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(mockPreProcessor);
    initVictim();
    final Resource resource = Resource.create("someResource.js");
    resource.setMinimize(false);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.atLeastOnce()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldInvokeMinimizeAwarePreProcessorWithResourceWantingMinimize()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(new MinimizeAwareProcessor());
    initVictim();
    final Resource resource = Resource.create("someResource.js");
    resource.setMinimize(true);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.never()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @Test
  public void shouldNotInvokeMinimizeAwarePreProcessorWithResourceNotWantingMinimize()
      throws Exception {
    victim = new MinimizeAwareProcessorDecorator(new MinimizeAwareProcessor());
    initVictim();
    final Resource resource = Resource.create("someResource.js");
    resource.setMinimize(false);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockPreProcessor, Mockito.never()).process(Mockito.any(Resource.class), Mockito.any(Reader.class),
        Mockito.any(Writer.class));
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
