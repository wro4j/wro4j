package ro.isdc.wro.model.resource.processor.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * @author Alex Objelean
 */
public class TestInjectorAwareProcessorsFactoryDecorator {
  private Injector injector;
  @Mock
  private ProcessorsFactory mockProcessorsFactory;
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullFactory() {
    new InjectorAwareProcessorsFactoryDecorator(null, injector);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullWithNullInjector() {
    new InjectorAwareProcessorsFactoryDecorator(mockProcessorsFactory, null);
  }
  
  @Test
  public void shouldInjectFieldsOfTheDecoratedFactory() {
    final SampleProcessorsFactory sampleFactory = new SampleProcessorsFactory();
    new InjectorAwareProcessorsFactoryDecorator(sampleFactory, injector);
    Assert.assertNotNull(sampleFactory.locatorFactory);
  }
  
  @Test
  public void shouldInjectFieldsOfAddedProcessors() {
    final SampleProcessor processor = new SampleProcessor();
    final ProcessorsFactory sampleFactory = new SimpleProcessorsFactory().addPostProcessor(processor);
    final ProcessorsFactory factory = new InjectorAwareProcessorsFactoryDecorator(sampleFactory, injector);
    //trigger injection processing
    factory.getPostProcessors();
    Assert.assertNotNull(processor.locatorFactory);
  }
  
  private static class SampleProcessorsFactory extends SimpleProcessorsFactory {
    @Inject
    ResourceLocatorFactory locatorFactory;
  }
  private static class SampleProcessor extends JSMinProcessor {
    @Inject
    ResourceLocatorFactory locatorFactory;
  }
}
