/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.ObjectFactory;


/**
 * @author Alex Objelean
 */
public class TestLifecycleCallbackDecorator {
  private LifecycleCallbackDecorator decorator;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    decorator = new LifecycleCallbackDecorator(null);
  }
  
  @Test
  public void shouldCatchCallbacksExceptionsAndContinueExecution() {
    final Resource changedResource = Resource.create("test.js");
    final LifecycleCallback callback = Mockito.spy(new PerformanceLoggerCallback());
    decorator = new LifecycleCallbackDecorator(callback);
    
    final LifecycleCallbackRegistry registry = new LifecycleCallbackRegistry();
    registry.registerCallback(new ObjectFactory<LifecycleCallback>() {
      public LifecycleCallback create() {
        return decorator;
      }
    });
    
    registry.onBeforeModelCreated();
    registry.onAfterModelCreated();
    registry.onBeforePreProcess();
    registry.onAfterPreProcess();
    registry.onBeforePostProcess();
    registry.onAfterPostProcess();
    registry.onBeforeMerge();
    registry.onAfterMerge();
    registry.onProcessingComplete();
    registry.onResourceChanged(changedResource);
    
    Mockito.verify(callback).onBeforeModelCreated();
    Mockito.verify(callback).onAfterModelCreated();
    Mockito.verify(callback).onBeforePreProcess();
    Mockito.verify(callback).onAfterPreProcess();
    Mockito.verify(callback).onBeforePostProcess();
    Mockito.verify(callback).onAfterPostProcess();
    Mockito.verify(callback).onBeforeMerge();
    Mockito.verify(callback).onAfterMerge();
    Mockito.verify(callback).onProcessingComplete();
    Mockito.verify(callback).onResourceChanged(Mockito.eq(changedResource));
  }
}
