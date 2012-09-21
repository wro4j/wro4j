package ro.isdc.wro.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

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
    victim.getAllResources(); 
  }
  
  @Test
  public void shouldReturnEmptyCollectionWhenAResourceIsNotContainedInAnyGroup() {
    assertTrue(victim.getGroupNamesContainingResource("/resourceMissingFromModel.js").isEmpty());
  }
  
  @Test
  public void shouldFindTheGroupContainingResource() {
    Collection<String> groups = victim.getGroupNamesContainingResource("/path/to/resource");
    assertEquals(2, groups.size());
    assertEquals("[g2, g3]", Arrays.toString(groups.toArray()));
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotGetGroupsUsingNullResource() {
    victim.getGroupNamesContainingResource(null);
  }
  
  
  @Test
  public void shouldReturnAllResourcesFromModel() {
    assertEquals(3, victim.getAllResources().size());
  }

  @Test
  public void testGetGroupNames() {
    final List<String> groupNames = victim.getGroupNames();
    Collections.sort(groupNames);
    final List<String> expected = Arrays.asList("g1", "g2", "g3");
    Assert.assertEquals(expected, groupNames);
  }
  
  
  @Test
  public void shouldNotReturnDuplicatedResources() {
    final WroModel model = new WroModel();
    victim = new WroModelInspector(model);
    
    assertEquals(0, victim.getAllResources().size());
    
    model.addGroup(new Group("one").addResource(Resource.create("/one.js"))).addGroup(
        new Group("two").addResource(Resource.create("/one.js")));
    assertEquals(1, victim.getAllResources().size());
  }

}
