package ro.isdc.wro.model.resource.support;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestDefaultResourceAuthorizationManager {
  private DefaultResourceAuthorizationManager victim;
  
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
}
