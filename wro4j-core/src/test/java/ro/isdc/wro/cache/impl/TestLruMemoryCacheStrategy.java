package ro.isdc.wro.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.support.hash.CRC32HashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * Tests the {@link LruMemoryCacheStrategy} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class TestLruMemoryCacheStrategy {
  private LruMemoryCacheStrategy<CacheKey, CacheValue> cache;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    cache = new LruMemoryCacheStrategy<CacheKey, CacheValue>(3);
  }

  @Test
  public void testLruCache() throws IOException {
    final HashStrategy builder = new CRC32HashStrategy();
    final CacheKey key1 = new CacheKey("testGroup01", ResourceType.JS, false);
    final CacheKey key2 = new CacheKey("testGroup02", ResourceType.CSS, false);
    final CacheKey key3 = new CacheKey("testGroup03", ResourceType.JS, false);
    final CacheKey key4 = new CacheKey("testGroup04", ResourceType.CSS, false);

    final String content = "var foo = 'Hello World';";
    final String hash = builder.getHash(new ByteArrayInputStream(content.getBytes()));

    cache.put(key1, CacheValue.valueOf(content, hash));
    cache.put(key2, CacheValue.valueOf(content, hash));
    cache.put(key3, CacheValue.valueOf(content, hash));
    assertNotNull(cache.get(key1));
    // Removes the 2nd entry because the 1st one was used in the assertion
    // above.
    cache.put(key4, CacheValue.valueOf(content, hash));
    assertNull(cache.get(key2));
  }


  @After
  public void tearDown() {
    Context.unset();
  }
}
