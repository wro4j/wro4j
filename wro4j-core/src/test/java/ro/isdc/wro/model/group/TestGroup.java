/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test class for {@link Group}.
 *
 * @author Alex Objelean
 */
public class TestGroup {
  private static final Logger LOG = LoggerFactory.getLogger(TestGroup.class);

  @Test(expected=NullPointerException.class)
  public void cannotCreateGroupWithNullName() {
    new Group(null);
  }

  @Test(expected=NullPointerException.class)
  public void cannotPassNullResourceType() {
    final Group group = new Group("group");
    group.hasResourcesOfType(null);
  }


  @Test
  public void testNoResorucesOfTypeFound() {
    final Group group = new Group("group");
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.CSS));
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.JS));
  }

  @Test
  public void testResoruceOfTypeFound() {
    final Group group = new Group("group");
    final List<Resource> resources = new ArrayList<Resource>();
    resources.add(Resource.create("/some.css", ResourceType.CSS));
    group.setResources(resources);
    Assert.assertEquals(true, group.hasResourcesOfType(ResourceType.CSS));
    Assert.assertEquals(false, group.hasResourcesOfType(ResourceType.JS));
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotReplaceMissingResource() {
    final Group group = new Group("group");
    group.replace(Resource.create("/path", ResourceType.JS), Arrays.asList(Resource.create("", ResourceType.JS)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReplaceWithEmptyCollection() {
    final Group group = new Group("group");
    final Resource resource = Resource.create("/path", ResourceType.JS);
    group.addResource(resource);
    group.replace(resource, Collections.EMPTY_LIST);
    Assert.assertTrue(group.getResources().isEmpty());
  }

  @Test
  public void testReplaceWithFewResources() {
    final Group group = new Group("group");
    final Resource resource = Resource.create("/static/*", ResourceType.JS);
    resource.setMinimize(false);
    group.addResource(resource);
    group.replace(
        resource,
        Arrays.asList(Resource.create("/static/one.js", ResourceType.JS),
            Resource.create("/static/two.js", ResourceType.JS)));
    Assert.assertEquals(2, group.getResources().size());
    Assert.assertEquals(resource.isMinimize(), group.getResources().get(0).isMinimize());
  }

  @Test
  public void shouldReplaceOnlyOneAndPreserveOtherResources() {
    final Group group = new Group("group");
    final Resource resource = Resource.create("/static/*", ResourceType.JS);

    final Resource r0 = Resource.create("/asset/1.js", ResourceType.JS);
    group.addResource(r0);

    final Resource r1 = Resource.create("/asset/2.js", ResourceType.JS);
    group.addResource(r1);

    group.addResource(resource);
    group.replace(
        resource,
        Arrays.asList(Resource.create("/static/one.js", ResourceType.JS),
            Resource.create("/static/two.js", ResourceType.JS)));
    Assert.assertEquals(4, group.getResources().size());
    Assert.assertEquals(r0, group.getResources().get(0));
    Assert.assertEquals(r1, group.getResources().get(1));
  }
}
