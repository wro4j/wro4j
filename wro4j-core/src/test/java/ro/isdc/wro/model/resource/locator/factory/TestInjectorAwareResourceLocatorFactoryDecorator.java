package ro.isdc.wro.model.resource.locator.factory;

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
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.ObjectDecorator;


/**
 * @author Alex Objelean
 */
public class TestInjectorAwareResourceLocatorFactoryDecorator {
  private Injector injector;
  @Mock
  private ResourceLocatorFactory mockLocatorFactory;
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullFactory() {
    InjectorAwareResourceLocatorFactoryDecorator.decorate(null, injector);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullWithNullInjector() {
    InjectorAwareResourceLocatorFactoryDecorator.decorate(mockLocatorFactory, null);
  }
  
  @Test
  public void shouldInjectFieldsOfTheDecoratedFactory() {
    final SampleUriLocatorFactory locatorFactory = new SampleUriLocatorFactory();
    InjectorAwareResourceLocatorFactoryDecorator.decorate(locatorFactory, injector);
    Assert.assertNotNull(locatorFactory.processorsFactory);
  }
  
  @Test
  public void shouldInjectFieldsOfAddedLocators() {
    final SampleLocator locator = new SampleLocator("/path");
    final ResourceLocatorFactory sampleFactory = new SampleUriLocatorFactory().addResourceLocator(locator);
    final ResourceLocatorFactory factory = InjectorAwareResourceLocatorFactoryDecorator.decorate(sampleFactory, injector);
    //trigger injection processing
    factory.getLocator("/uri");
    Assert.assertNotNull(locator.processorsFactory);
  }
  

  @Test
  public void shouldDecorateOnce() {
    final ResourceLocatorFactory original = new SampleUriLocatorFactory();
    final ResourceLocatorFactory factory = InjectorAwareResourceLocatorFactoryDecorator.decorate(original, injector);
    Assert.assertTrue(factory instanceof InjectorAwareResourceLocatorFactoryDecorator);
    Assert.assertSame(original, ((ObjectDecorator<?>) factory).getDecoratedObject());
  }
  
  @Test
  public void shouldNotRedundantlyDecorate() {
    final ResourceLocatorFactory original = InjectorAwareResourceLocatorFactoryDecorator.decorate(new SampleUriLocatorFactory(), injector);
    final ResourceLocatorFactory factory = InjectorAwareResourceLocatorFactoryDecorator.decorate(original, injector);
    Assert.assertTrue(factory instanceof InjectorAwareResourceLocatorFactoryDecorator);
    Assert.assertSame(original, factory);
  }
  
  private static class SampleUriLocatorFactory
      extends AbstractResourceLocatorFactory {
    private ResourceLocator locator;
    @Inject
    ProcessorsFactory processorsFactory;
    public ResourceLocator getLocator(final String uri) {
      return locator;
    }
    public ResourceLocatorFactory addResourceLocator(final ResourceLocator locator) {
      this.locator = locator;
      return this;
    }
  }
  
  private static class SampleLocator extends ClasspathResourceLocator {
    public SampleLocator(final String path) {
      super(path);
    }

    @Inject
    ProcessorsFactory processorsFactory;
  }
}
