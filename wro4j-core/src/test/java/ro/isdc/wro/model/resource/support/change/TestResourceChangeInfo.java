package ro.isdc.wro.model.resource.support.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestResourceChangeInfo {
  private static final String GROUP1_NAME = "g1";
  private static final String GROUP2_NAME = "g2";
  private static final String GROUP3_NAME = "g3";
  private ResourceChangeInfo victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    victim = new ResourceChangeInfo();
  }


  @Test(expected = NullPointerException.class)
  public void cannotCheckChangeForNullGroupName() {
    victim.isChanged(null);
  }

  @Test
  public void shouldDetectChangeByDefault() {
    assertTrue(victim.isChanged(GROUP1_NAME));
    assertFalse(victim.isChanged(GROUP1_NAME));
  }

  @Test(expected = NullPointerException.class)
  public void cannotUpdateHashForNullGroup() {
    victim.updateHashForGroup("", null);
  }

  @Test
  public void shouldDetectChangeAfterHashChanged() {
    assertTrue(victim.isChanged(GROUP1_NAME));
    assertTrue(victim.isCheckRequiredForGroup(GROUP1_NAME));
    victim.updateHashForGroup("hash", GROUP1_NAME);
    assertTrue(victim.isChanged(GROUP1_NAME));
    victim.reset();
    assertTrue(victim.isChanged(GROUP1_NAME));
    assertTrue(victim.isChanged(GROUP2_NAME));
  }

  @Test(expected = NullPointerException.class)
  public void cannotIfCheckRequiredForNullGroup() {
    victim.isCheckRequiredForGroup(null);
  }

  @Test
  public void shouldRequireChangeByDefault() {
    assertTrue(victim.isCheckRequiredForGroup(GROUP1_NAME));
  }

  @Test
  public void shouldNotRequireChangeAfterHashUpdate() {
    victim.updateHashForGroup("hash", GROUP1_NAME);
    assertFalse(victim.isCheckRequiredForGroup(GROUP1_NAME));
  }

  @Test
  public void shouldDetectChangeForDifferentGroups() {
    victim.updateHashForGroup("hash1", GROUP1_NAME);
    victim.reset();
    assertTrue(victim.isChanged(GROUP1_NAME));

    assertTrue(victim.isCheckRequiredForGroup(GROUP2_NAME));
    assertTrue(victim.isChanged(GROUP2_NAME));
  }

  @Test
  public void shouldDetectChangeAfterNewHashUpdate() {
    victim.updateHashForGroup("hash1", GROUP1_NAME);
    assertTrue(victim.isChanged(GROUP1_NAME));
    victim.updateHashForGroup("hash1", GROUP2_NAME);
    assertTrue(victim.isChanged(GROUP2_NAME));
    assertTrue(victim.isChanged(GROUP1_NAME));
    assertTrue(victim.isChanged(GROUP1_NAME));
  }

  @Test
  public void shouldDetectChangeForUpdatedGroupsOnly() {
    victim.updateHashForGroup("hash1", GROUP1_NAME);
    victim.updateHashForGroup("hash1", GROUP2_NAME);
    victim.reset();
    assertTrue(victim.isChanged(GROUP1_NAME));
    assertTrue(victim.isChanged(GROUP2_NAME));
    victim.updateHashForGroup("hash1", GROUP1_NAME);
    victim.updateHashForGroup("hash1", GROUP2_NAME);
    assertFalse(victim.isChanged(GROUP1_NAME));
    assertFalse(victim.isChanged(GROUP2_NAME));
    assertTrue(victim.isChanged(GROUP3_NAME));
  }
}
