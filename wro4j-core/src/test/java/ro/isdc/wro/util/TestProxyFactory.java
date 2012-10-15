package ro.isdc.wro.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


/**
 * @author Alex Objelean
 */
public class TestProxyFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestProxyFactory.class);
  private ProxyFactory<?> victim;

  @Test
  public void shouldCreateProxyForAnObjectWithNotAAnInterfaceType() {
    victim = new ProxyFactory<Object>(new Object(), Object.class);
    LOG.debug("Proxy: {}", victim.create());
  }

  @Test
  public void shouldCreateProxyForNotAnInterface() {
    victim = new ProxyFactory<Object>(new Object());
    assertNotNull(victim.create());
  }

  @Test
  public void shouldCreateProxyForAValidObject() {
    final ReadOnlyContext object = Context.standaloneContext();
    victim = new ProxyFactory<ReadOnlyContext>(object, ReadOnlyContext.class);
    assertNotNull(victim.create());
    assertNotSame(object, victim.create());
  }

  @Test
  public void shouldCreateProxyForAValidObjectWithNoTypeProvided() {
    final ReadOnlyContext object = Context.standaloneContext();
    victim = new ProxyFactory<ReadOnlyContext>(object);
    assertNotNull(victim.create());
    assertNotSame(object, victim.create());
  }

  @Test
  public void shouldInheritInterfacesOfTheObject() {
    final ResourceAuthorizationManager object = new DefaultResourceAuthorizationManager();
    victim = new ProxyFactory<ResourceAuthorizationManager>(object);
    final Object proxy = victim.create();
    assertNotNull(proxy);
    assertNotSame(object, proxy);
    assertTrue(proxy instanceof MutableResourceAuthorizationManager);
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
