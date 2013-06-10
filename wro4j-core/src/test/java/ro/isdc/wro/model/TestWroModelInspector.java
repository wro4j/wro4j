package ro.isdc.wro.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;

/**
 * @author Alex Objelean
 */
public class TestWroModelInspector {
  private WroModelInspector victim;

  @Before
  public void setUp() {
    victim = new WroModelInspector(buildValidModel());
  }

  /**
   * @return a valid {@link WroModel} pre populated with some valid resources.
   */
  private WroModel buildValidModel() {
    final WroModelFactory factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return getClass().getResourceAsStream("modelInspector.xml");
      }
    };
    return factory.create();
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullModel() {
    new WroModelInspector(null);
  }

  public void test() {
    victim.getAllUniqueResources();
  }

  @Test
  public void shouldReturnEmptyCollectionWhenAResourceIsNotContainedInAnyGroup() {
    assertTrue(victim.getGroupNamesContainingResource("/resourceMissingFromModel.js").isEmpty());
  }

  @Test
  public void shouldFindTheGroupContainingResource() {
    final Collection<String> groups = victim.getGroupNamesContainingResource("/path/to/resource");
    assertEquals(2, groups.size());
    assertEquals("[g2, g3]", Arrays.toString(groups.toArray()));
  }

  @Test
  public void shouldGetGroupNamesAsString() {
    assertEquals("g1, g2, g3", victim.getGroupNamesAsString());
  }

  @Test(expected = NullPointerException.class)
  public void cannotGetGroupsUsingNullResource() {
    victim.getGroupNamesContainingResource(null);
  }


  @Test
  public void shouldReturnAllResourcesFromModel() {
    assertEquals(3, victim.getAllUniqueResources().size());
  }

  @Test
  public void testGetGroupNames() {
    final List<String> groupNames = victim.getGroupNames();
    Collections.sort(groupNames);
    final List<String> expected = Arrays.asList("g1", "g2", "g3");
    Assert.assertEquals(expected, groupNames);
  }


  /**
   * Proves that inspector works only with model snapshot and does not reflect model changes performed after inspector
   * is constructed.
   */
  @Test
  public void shouldReturnSameResultAfterModelChange() {
    final WroModel model = new WroModel();
    victim = new WroModelInspector(model);

    assertEquals(0, victim.getAllUniqueResources().size());

    model.addGroup(new Group("one").addResource(Resource.create("/one.js"))).addGroup(
        new Group("two").addResource(Resource.create("/one.js")));
    //should still be zero, even if the model changed
    assertEquals(0, victim.getAllUniqueResources().size());
    assertEquals(1, new WroModelInspector(model).getAllUniqueResources().size());
    assertEquals(2, new WroModelInspector(model).getAllResources().size());
  }

  @Test
  public void testHasGroup() {
    assertFalse(victim.hasGroup("NOT_EXIST"));
    assertTrue(victim.hasGroup("g1"));
  }

}
