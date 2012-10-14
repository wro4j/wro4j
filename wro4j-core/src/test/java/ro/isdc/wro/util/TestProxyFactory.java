package ro.isdc.wro.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;


/**
 * @author Alex Objelean
 */
public class TestProxyFactory {
  private ProxyFactory<?> victim;

  @Test(expected = IllegalArgumentException.class)
  public void cannotCreateProxyForANoneInterface() {
    victim = new ProxyFactory<Object>(new ObjectFactory<Object>() {
      public Object create() {
        return new Object();
      }
    }, Object.class);
    victim.create();
  }

  @Test
  public void shouldCreateProxyForAValidObjectFactory() {
    final ReadOnlyContext object = Context.standaloneContext();
    victim = new ProxyFactory<ReadOnlyContext>(new ObjectFactory<ReadOnlyContext>() {
      public ReadOnlyContext create() {
        return object;
      }
    }, ReadOnlyContext.class);
    assertNotNull(victim.create());
    assertNotSame(object, victim.create());
  }


  @Test
  public void shouldCreateProxyForAValidObject() {
    final ReadOnlyContext object = Context.standaloneContext();
    victim = new ProxyFactory<ReadOnlyContext>(object, ReadOnlyContext.class);
    assertNotNull(victim.create());
    assertNotSame(object, victim.create());
  }


  @Test(expected = NullPointerException.class)
  public void shouldCreateProxyForNullObject() {
    victim = new ProxyFactory<ReadOnlyContext>(new ObjectFactory<ReadOnlyContext>() {
      public ReadOnlyContext create() {
        return null;
      }
    }, ReadOnlyContext.class);
    // System.out.println(victim.create());
    final ReadOnlyContext proxy = (ReadOnlyContext) victim.create();
    assertNotNull(proxy);
    assertNotNull(proxy.getConfig());
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullObjectFactory() {
    new ProxyFactory<Object>(null, Object.class);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullObject() {
    new ProxyFactory<Object>(null, Object.class);
  }
}
