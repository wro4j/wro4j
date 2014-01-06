package ro.isdc.wro.cache.impl;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
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
 * Tests the {@link MemoryCacheStrategy} class.
 * 
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class TestMemoryCacheStrategy {
  private MemoryCacheStrategy<CacheKey, CacheValue> cache;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    cache = new MemoryCacheStrategy<CacheKey, CacheValue>();
  }
  
  @Test
  public void testCache()
      throws IOException {
    final HashStrategy builder = new CRC32HashStrategy();
    final CacheKey key = new CacheKey("testGroup", ResourceType.JS, false);
    
    final String content = "var foo = 'Hello World';";
    final String hash = builder.getHash(new ByteArrayInputStream(content.getBytes()));
    
    Assert.assertNull(cache.get(key));
    
    cache.put(key, CacheValue.valueOf(content, hash));
    
    final CacheValue entry = cache.get(key);
    
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
