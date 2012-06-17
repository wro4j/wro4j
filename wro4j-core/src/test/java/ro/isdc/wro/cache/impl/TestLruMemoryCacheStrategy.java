package ro.isdc.wro.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.support.hash.CRC32HashBuilder;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * Tests the {@link LruMemoryCacheStrategy} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class TestLruMemoryCacheStrategy {
  private LruMemoryCacheStrategy<CacheEntry, ContentHashEntry> cache;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    cache = new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>(3);
  }

  @Test
  public void testLruCache() throws IOException {
    HashStrategy builder = new CRC32HashBuilder();
    CacheEntry key1 = new CacheEntry("testGroup01", ResourceType.JS, false);
    CacheEntry key2 = new CacheEntry("testGroup02", ResourceType.CSS, false);
    CacheEntry key3 = new CacheEntry("testGroup03", ResourceType.JS, false);
    CacheEntry key4 = new CacheEntry("testGroup04", ResourceType.CSS, false);

    String content = "var foo = 'Hello World';";
    String hash = builder.getHash(new ByteArrayInputStream(content.getBytes()));

    cache.put(key1, ContentHashEntry.valueOf(content, hash));
    cache.put(key2, ContentHashEntry.valueOf(content, hash));
    cache.put(key3, ContentHashEntry.valueOf(content, hash));
    Assert.assertNotNull(cache.get(key1));
    // Removes the 2nd entry because the 1st one was used in the assertion
    // above.
    cache.put(key4, ContentHashEntry.valueOf(content, hash));
    Assert.assertNull(cache.get(key2));
  }
  

  @After
  public void tearDown() {
    Context.unset();
  }
}
