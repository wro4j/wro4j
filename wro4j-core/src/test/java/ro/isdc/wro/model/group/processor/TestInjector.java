/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
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

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullManager() {
    injector = new Injector(null);
  }

  @Test
  public void shouldAcceptInjectInitializedManager() {
    initializeValidInjector();
  }

  private void initializeValidInjector() {
    final WroManager manager = new BaseWroManagerFactory().create();
    injector = new InjectorBuilder(manager).build();
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotInjectUnsupportedType() {
    initializeValidInjector();
    final Object inner = new Object() {
      @Inject
      private Object object;
    };
    injector.inject(inner);
  }


  @Test
  public void shouldInjectSupportedType() throws Exception {
    initializeValidInjector();
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private LifecycleCallbackRegistry object;
      public Void call()
        throws Exception {
        Assert.assertNotNull(object);
        return null;
      }
    };
    injector.inject(inner);
    inner.call();
  }

  @Test
  public void shouldInjectContext() throws Exception {
    // Cannot reuse this part, because generic type is not inferred correcty at runtime
    initializeValidInjector();
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private Context object;
      public Void call()
        throws Exception {
        Assert.assertNotNull(object);
        return null;
      }
    };
    injector.inject(inner);
    inner.call();
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotInjectContextOutsideOfContextScope() throws Exception {
    //remove the context explicitly
    Context.unset();
    shouldInjectContext();
  }

  @Test
  public void shouldInjectWroConfiguration() throws Exception {
    initializeValidInjector();
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private WroConfiguration object;
      public Void call()
        throws Exception {
        Assert.assertNotNull(object);
        return null;
      }
    };
    injector.inject(inner);
    inner.call();
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
