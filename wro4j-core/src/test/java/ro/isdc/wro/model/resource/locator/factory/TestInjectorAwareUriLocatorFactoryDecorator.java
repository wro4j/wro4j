package ro.isdc.wro.model.resource.locator.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * @author Alex Objelean
 */
public class TestInjectorAwareUriLocatorFactoryDecorator {
  private Injector injector;
  @Mock
  private ResourceLocatorFactory mockLocatorFactory;
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = new InjectorBuilder().build();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullFactory() {
    new InjectorAwareResourceLocatorFactoryDecorator(null, injector);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullWithNullInjector() {
    new InjectorAwareResourceLocatorFactoryDecorator(mockLocatorFactory, null);
  }
  
  @Test
  public void shouldInjectFieldsOfTheDecoratedFactory() {
    final SampleUriLocatorFactory locatorFactory = new SampleUriLocatorFactory();
    new InjectorAwareResourceLocatorFactoryDecorator(locatorFactory, injector);
    Assert.assertNotNull(locatorFactory.processorsFactory);
  }
  
  @Test
  public void shouldInjectFieldsOfAddedLocators() {
    final SampleLocator locator = new SampleLocator("/uri");
    final ResourceLocatorFactory sampleFactory = new SampleUriLocatorFactory().addResourceLocator(locator);
    final ResourceLocatorFactory factory = new InjectorAwareResourceLocatorFactoryDecorator(sampleFactory, injector);
    //trigger injection processing
    factory.locate("/uri");
    Assert.assertNotNull(locator.processorsFactory);
  }
  
  
  private static class SampleUriLocatorFactory implements ResourceLocatorFactory {
    private ResourceLocator locator;
    @Inject
    ProcessorsFactory processorsFactory;
    public ResourceLocator locate(String uri) {
      return locator;
    }
    public ResourceLocatorFactory addResourceLocator(ResourceLocator locator) {
      this.locator = locator;
      return this;
    }
  }
  
  private static class SampleLocator extends ClasspathResourceLocator {
    public SampleLocator(String path) {
      super(path);
    }

    @Inject
    ProcessorsFactory processorsFactory;
  }
}
