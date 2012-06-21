package ro.isdc.wro.model.resource.support;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestResourceAuthorizationManager {
  private ResourceAuthorizationManager victim;
  
  @Before
  public void setUp() {
    victim = new ResourceAuthorizationManager();
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
}
