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
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    injector = new InjectorBuilder().build();
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
  public void test() {
    final SampleUriLocatorFactory locatorFactory = new SampleUriLocatorFactory();
    new InjectorAwareUriLocatorFactoryDecorator(locatorFactory, injector);
    Assert.assertNotNull(locatorFactory.processorsFactory);
  }
  
  private static class SampleUriLocatorFactory extends SimpleUriLocatorFactory {
    @Inject
    ProcessorsFactory processorsFactory;
  }
}
