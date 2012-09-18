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
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.ObjectDecorator;


/**
 * @author Alex Objelean
 */
public class TestInjectorAwareUriLocatorFactoryDecorator {
  private Injector injector;
  @Mock
  private UriLocatorFactory mockLocatorFactory;
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullFactory() {
    InjectorAwareUriLocatorFactoryDecorator.decorate(null, injector);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullWithNullInjector() {
    InjectorAwareUriLocatorFactoryDecorator.decorate(mockLocatorFactory, null);
  }
  
  @Test
  public void shouldInjectFieldsOfTheDecoratedFactory() {
    final SampleUriLocatorFactory locatorFactory = new SampleUriLocatorFactory();
    InjectorAwareUriLocatorFactoryDecorator.decorate(locatorFactory, injector);
    Assert.assertNotNull(locatorFactory.processorsFactory);
  }
  
  @Test
  public void shouldInjectFieldsOfAddedLocators() {
    final SampleLocator locator = new SampleLocator();
    final UriLocatorFactory sampleFactory = new SimpleUriLocatorFactory().addUriLocator(locator);
    final UriLocatorFactory factory = InjectorAwareUriLocatorFactoryDecorator.decorate(sampleFactory, injector);
    //trigger injection processing
    factory.getInstance("/uri");
    Assert.assertNotNull(locator.processorsFactory);
  }
  

  @Test
  public void shouldDecorateOnce() {
    final UriLocatorFactory original = new SimpleUriLocatorFactory();
    final UriLocatorFactory factory = InjectorAwareUriLocatorFactoryDecorator.decorate(original, injector);
    Assert.assertTrue(factory instanceof InjectorAwareUriLocatorFactoryDecorator);
    Assert.assertSame(original, ((ObjectDecorator<?>) factory).getDecoratedObject());
  }
  
  @Test
  public void shouldNotRedundantlyDecorate() {
    final UriLocatorFactory original = InjectorAwareUriLocatorFactoryDecorator.decorate(new SimpleUriLocatorFactory(), injector);
    final UriLocatorFactory factory = InjectorAwareUriLocatorFactoryDecorator.decorate(original, injector);
    Assert.assertTrue(factory instanceof InjectorAwareUriLocatorFactoryDecorator);
    Assert.assertSame(original, factory);
  }
  
  private static class SampleUriLocatorFactory extends SimpleUriLocatorFactory {
    @Inject
    ProcessorsFactory processorsFactory;
  }
  
  private static class SampleLocator extends ServletContextUriLocator {
    @Inject
    ProcessorsFactory processorsFactory;
  }
}
