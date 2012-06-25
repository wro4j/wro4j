package ro.isdc.wro.model.resource.locator.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.DefaultContext;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * @author Alex Objelean
 */
public class TestInjectorAwareUriLocatorFactoryDecorator {
  private Injector injector;
  @Mock
  private UriLocatorFactory mockLocatorFactory;
  
  @Before
  public void setUp() {
    DefaultContext.set(DefaultContext.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullFactory() {
    new InjectorAwareUriLocatorFactoryDecorator(null, injector);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullWithNullInjector() {
    new InjectorAwareUriLocatorFactoryDecorator(mockLocatorFactory, null);
  }
  
  @Test
  public void shouldInjectFieldsOfTheDecoratedFactory() {
    final SampleUriLocatorFactory locatorFactory = new SampleUriLocatorFactory();
    new InjectorAwareUriLocatorFactoryDecorator(locatorFactory, injector);
    Assert.assertNotNull(locatorFactory.processorsFactory);
  }
  
  @Test
  public void shouldInjectFieldsOfAddedLocators() {
    final SampleLocator locator = new SampleLocator();
    final UriLocatorFactory sampleFactory = new SimpleUriLocatorFactory().addUriLocator(locator);
    final UriLocatorFactory factory = new InjectorAwareUriLocatorFactoryDecorator(sampleFactory, injector);
    //trigger injection processing
    factory.getInstance("/uri");
    Assert.assertNotNull(locator.processorsFactory);
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
