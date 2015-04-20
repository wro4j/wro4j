/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test class for {@link Group}.
 *
 * @author Alex Objelean
 */
public class TestGroup {
  private static Random RANDOM;
  @BeforeClass
  public static void onBeforeClass() {
    RANDOM = new Random();
    assertEquals(0, Context.countActive());
  }

  @Test(expected = NullPointerException.class)
  public void cannotCreateGroupWithNullName() {
    new Group(null);
  }

  @Test(expected = NullPointerException.class)
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

  @Test(expected = IllegalArgumentException.class)
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
  public void shouldReplaceAResourceWithSameResource() {
    final Group group = new Group("group");
    final Resource resource = Resource.create("/path.js");
    group.addResource(resource);

    final List<Resource> resourceList = new ArrayList<Resource>();
    resourceList.add(resource);

    group.replace(resource, resourceList);
    Assert.assertFalse(group.getResources().isEmpty());
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

  @Test(expected = NullPointerException.class)
  public void cannotCollectResourcesWithNullType() {
    final Group group = new Group("group");
    group.collectResourcesOfType(null);
  }

  @Test
  public void shouldCollectCorrectNumberOfResourcesByType() {
    final Group group = new Group("group");
    group.addResource(Resource.create("1.js"));
    group.addResource(Resource.create("2.js"));
    group.addResource(Resource.create("3.js"));
    group.addResource(Resource.create("4.js"));
    group.addResource(Resource.create("5.js"));
    group.addResource(Resource.create("6.js"));
    group.addResource(Resource.create("1.css"));

    Assert.assertEquals(6, group.collectResourcesOfType(ResourceType.JS).getResources().size());
    Assert.assertEquals(1, group.collectResourcesOfType(ResourceType.CSS).getResources().size());
  }

  @Test
  public void shouldBeThreadSafeWhenMutated()
      throws Exception {
    final Group group = new Group("group");
    final List<Resource> resources = new ArrayList<Resource>();
    final Resource r1 = Resource.create("/some.css", ResourceType.CSS);
    resources.add(r1);

    WroTestUtils.runConcurrently(new Callable<Void>() {
      public Void call()
          throws Exception {
        if (RANDOM.nextBoolean()) {
          group.setResources(resources);
        } else {
          group.addResource(r1);
          group.replace(r1, resources);
        }
        return null;
      }
    });
  }
}
