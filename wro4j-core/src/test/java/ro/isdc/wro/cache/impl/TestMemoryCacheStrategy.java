package ro.isdc.wro.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.support.hash.CRC32HashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * Tests the {@link MemoryCacheStrategy} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class TestMemoryCacheStrategy {
  private MemoryCacheStrategy<CacheKey, CacheValue> cache;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    cache = new MemoryCacheStrategy<CacheKey, CacheValue>();
  }

  @Test
  public void testCache() throws IOException {
    HashStrategy builder = new CRC32HashStrategy();
    CacheKey key = new CacheKey("testGroup", ResourceType.JS, false);

    String content = "var foo = 'Hello World';";
    String hash = builder.getHash(new ByteArrayInputStream(content.getBytes()));

    Assert.assertNull(cache.get(key));

    cache.put(key, CacheValue.valueOf(content, hash));

    CacheValue entry = cache.get(key);

    Assert.assertNotNull(entry);
    Assert.assertEquals(hash, entry.getHash());
    Assert.assertEquals(content, entry.getRawContent());

    cache.clear();
    Assert.assertNull(cache.get(key));

    cache.put(key, CacheValue.valueOf(content, hash));
    Assert.assertNotNull(cache.get(key));
    cache.destroy();
    Assert.assertNull(cache.get(key));
  }
  

  @After
  public void tearDown() {
    Context.unset();
  }
}
