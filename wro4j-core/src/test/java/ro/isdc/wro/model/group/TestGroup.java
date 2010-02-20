/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test class for {@link Group}.
 *
 * @author Alex Objelean
 */
public class TestGroup {
  @Test(expected=IllegalArgumentException.class)
  public void cannotPassNullResourceType() {
    final Group group = new Group();
    group.hasResourcesOfType(null);
  }

  @Test
  public void testNoResorucesOfTypeFound() {
    final Group group = new Group();
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.CSS));
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.JS));
  }

  @Test
  public void testResoruceOfTypeFound() {
    final Group group = new Group();
    final List<Resource> resources = new ArrayList<Resource>();
    resources.add(Resource.create("/some.css", ResourceType.CSS));
    group.setResources(resources);
    Assert.assertEquals(true, group.hasResourcesOfType(ResourceType.CSS));
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.JS));
  }
}
