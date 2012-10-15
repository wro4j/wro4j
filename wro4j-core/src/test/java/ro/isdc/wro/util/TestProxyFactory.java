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
  public void shouldCreateProxyForAValidObject() {
    final ReadOnlyContext object = Context.standaloneContext();
    victim = new ProxyFactory<ReadOnlyContext>(object, ReadOnlyContext.class);
    assertNotNull(victim.create());
    assertNotSame(object, victim.create());
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
