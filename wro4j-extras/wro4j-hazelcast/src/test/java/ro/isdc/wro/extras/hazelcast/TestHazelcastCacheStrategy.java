package ro.isdc.wro.extras.hazelcast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestHazelcastCacheStrategy {
  private static HazelcastCacheStrategy<String, String> victim;
  @BeforeClass
  public static void setUpClass() {
    victim = new HazelcastCacheStrategy<String, String>();
  }

  @Test
  public void shouldFindExistingKey() {
    victim.put("k1", "v1");
    assertEquals("v1", victim.get("k1"));
  }

  @Test
  public void shouldRemoveExistingKey() {
    victim.put("k1", "v1");
    assertEquals("v1", victim.get("k1"));
    victim.put("k1", null);
    assertNull(victim.get("k1"));
  }

  @Test
  public void shouldAllowAddingNullKey() {
    final String value = "value";
    victim.put(null, value);
    assertNull(victim.get(null));
  }

  @Test
  public void shouldFindExistingKeyForCustomNamedStrategy() {
    final HazelcastCacheStrategy<String, String> victim = new HazelcastCacheStrategy<String, String>("custom");
    victim.put("k1", "v1");
    assertEquals("v1", victim.get("k1"));
  }
}
