package ro.isdc.wro.model.resource.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestDefaultResourceAuthorizationManager {
  private DefaultResourceAuthorizationManager victim;

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
    victim = new DefaultResourceAuthorizationManager();
  }

  @Test(expected = NullPointerException.class)
  public void cannotAddNullResource() {
    victim.add(null);
  }

  @Test
  public void shouldAuthorizeAddedResource() {
    final String resource = "/resource.js";
    victim.add(resource);
    assertTrue(victim.isAuthorized(resource));
  }

  @Test
  public void shouldNotAuthorizeAddedResourceAfterClearIsInvoked() {
    final String resource = "/resource.js";
    victim.add(resource);
    assertTrue(victim.isAuthorized(resource));
    victim.clear();
    assertFalse(victim.isAuthorized(resource));
  }

  @Test
  public void shouldContainOnlyOneResourceWhenSameIsAddedTwice() {
    final String resource = "/resource.js";
    victim.add(resource);
    victim.add(resource);
    assertEquals(1, victim.list().size());
    assertEquals(resource, victim.list().iterator().next());
  }

  @Test
  public void shoudlIgnoreQueryPathWhenResourceIsAdded() {
    victim.add("classpath:META-INF/resources/fonts/glyphicons-halflings-regular.eot?#iefix");
    assertTrue(victim.isAuthorized("classpath:META-INF/resources/fonts/glyphicons-halflings-regular.eot"));
  }

  @Test
  public void shoudlIgnoreQueryPathWhenResourceCheckedForAuthorization() {
    victim.add("classpath:META-INF/resources/fonts/glyphicons-halflings-regular.eot");
    assertTrue(victim.isAuthorized("classpath:META-INF/resources/fonts/glyphicons-halflings-regular.eot?#iefix"));
  }
}
