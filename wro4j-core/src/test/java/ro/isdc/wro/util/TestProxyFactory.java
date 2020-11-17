package ro.isdc.wro.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.Validate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.ProxyFactory.TypedObjectFactory;


/**
 * @author Alex Objelean
 */
public class TestProxyFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestProxyFactory.class);

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @Test
  public void shouldCreateProxyForAnObjectWithNotAAnInterfaceType() {
    final Object proxy = ProxyFactory.proxy(new TypedObjectFactory<Object>() {
      public Object create() {
        return new Object();
      }

      public Class<? extends Object> getObjectClass() {
        return Object.class;
      }
    }, Object.class);
    Validate.notNull(proxy);
    LOG.debug("Proxy: {}", proxy);
  }

  @Test(expected = NullPointerException.class)
  public void cannotCreateProxyFromNullObjectFactory() {
    ProxyFactory.proxy(null, Object.class);
  }

  @Test
  public void shouldCreateProxyForAValidObject() {
    final ReadOnlyContext object = Context.standaloneContext();
    final ReadOnlyContext proxy = ProxyFactory.proxy(new TypedObjectFactory<ReadOnlyContext>() {
      public ReadOnlyContext create() {
        return object;
      }

      public Class<? extends ReadOnlyContext> getObjectClass() {
        return ReadOnlyContext.class;
      }
    }, ReadOnlyContext.class);
    assertNotNull(proxy);
    assertNotSame(object, proxy);
  }

  @Test
  public void shouldInheritInterfacesOfTheObject() {
    final ResourceAuthorizationManager object = new DefaultResourceAuthorizationManager();
    final ResourceAuthorizationManager proxy = ProxyFactory.proxy(
        new TypedObjectFactory<ResourceAuthorizationManager>() {
          public ResourceAuthorizationManager create() {
            return object;
          }

          public Class<? extends ResourceAuthorizationManager> getObjectClass() {
            return DefaultResourceAuthorizationManager.class;
          }
        }, ResourceAuthorizationManager.class);
    assertNotNull(proxy);
    assertNotSame(object, proxy);
    assertTrue(proxy instanceof MutableResourceAuthorizationManager);
  }
}
