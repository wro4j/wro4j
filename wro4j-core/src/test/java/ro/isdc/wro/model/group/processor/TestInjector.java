/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Inject;

/**
 * @author Alex Objelean
 * @created 12 Dec 2011
 */
public class TestInjector {
  private Injector injector;
  
  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullManager() {
    injector = new Injector(null);
  }
  
  @Test(expected=NullPointerException.class)
  public void cannotAcceptUninitializedManager() {
    injector = new Injector(Mockito.mock(WroManager.class));
  }

  @Test
  public void shouldAcceptInjectInitializedManager() {
    initializeValidInjector();
  }

  private void initializeValidInjector() {
    WroManager manager = new BaseWroManagerFactory().create();
    injector = new Injector(manager);
  }
  
  @Test(expected=WroRuntimeException.class)
  public void cannotInjectUnsupportedType() {
    initializeValidInjector();
    Object inner = new Object() {
      @Inject
      private Object object;
    };
    injector.inject(inner);
  }
  

  @Test
  public void shouldInjectSupportedType() {
    initializeValidInjector();
    Object inner = new Object() {
      @Inject
      private LifecycleCallbackRegistry callbackRegistry;
    };
    injector.inject(inner);
  }
}
