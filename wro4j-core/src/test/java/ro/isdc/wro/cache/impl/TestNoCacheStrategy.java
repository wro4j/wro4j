package ro.isdc.wro.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

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
 * Tests the {@link NoCacheStrategy} class.
 *
 * @author Philippe Da Costa &lt;pdacosta@gmail.com&gt;
 */
public class TestNoCacheStrategy {
  private NoCacheStrategy<CacheKey, CacheValue> cache;
  
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
    cache = new NoCacheStrategy<CacheKey, CacheValue>();
  }
  
  @Test
  public void shouldNotKeepStoredValue()
      throws Exception {
    HashStrategy builder = new CRC32HashStrategy();
    CacheKey key = new CacheKey("testGroup", ResourceType.JS, false);
    
    String content = "var foo = 'Hello World';";
    String hash = builder.getHash(new ByteArrayInputStream(content.getBytes()));
    
    assertNull(cache.get(key));
    cache.put(key, CacheValue.valueOf(content, hash));
    assertNull(cache.get(key));
    
    cache.clear();
    cache.destroy();
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
}