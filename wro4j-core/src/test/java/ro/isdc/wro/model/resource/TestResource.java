/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestResource {
  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullUri() {
    Resource.create(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullUri2() {
    Resource.create(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullResourceType() {
    Resource.create("resource.js", null);
  }

  @Test
  public void shouldCreateResource() {
    Assert.assertNotNull(Resource.create("resource.js", ResourceType.JS));
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotCreateResourceWithUnknownExtension() {
    Assert.assertNotNull(Resource.create("resource.coffee"));
  }

  @Test
  public void shouldCreateResourceWithCorrectType() {
    Resource resource = Resource.create("resource.js");
    Assert.assertNotNull(resource);
    Assert.assertEquals(ResourceType.JS, resource.getType());

    resource = Resource.create("resource.css");
    Assert.assertNotNull(resource);
    Assert.assertEquals(ResourceType.CSS, resource.getType());
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullType() {
    final Resource resource = new Resource();
    resource.setType(null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullUri() {
    final Resource resource = new Resource();
    resource.setUri(null);
  }

  @Test
  public void shouldSetMinimizeFlag() {
    final Resource resource = Resource.create("resource.js");
    resource.setMinimize(true);
    assertTrue(resource.isMinimize());
    resource.setMinimize(false);
    assertFalse(resource.isMinimize());
  }

  @Test
  public void shouldAllowSettingResourceTypeAndUri() {
    final Resource resource = new Resource();
    final String uri = "/someUri";
    resource.setType(ResourceType.CSS);
    resource.setUri(uri);
    assertEquals(ResourceType.CSS, resource.getType());
    assertEquals(uri, resource.getUri());
  }

}
