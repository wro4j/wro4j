/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Test class for MapCacheStrategy.
 *
 * @author Alex Objelean
 */
public class TestMemoryCacheStrategy {
  private CacheStrategy<CacheKey, String> strategy;

  @Before
  public void setUp() {
    strategy = new MemoryCacheStrategy<CacheKey, String>();
  }

  @Test
  public void testPut() {
    final CacheKey key = new CacheKey("g1", ResourceType.JS, true);
    final String value = "value";
    strategy.put(key, value);
    assertEquals(value, strategy.get(key));
  }

  @Test
  public void shouldRemoveExistingKey() {
    final CacheKey key = new CacheKey("g1", ResourceType.JS, true);
    final String value = "value";
    strategy.put(key, value);
    strategy.put(key, null);
    assertNull(strategy.get(key));
  }

  @Test
  public void shouldAllowAddingNullKey() {
    final String value = "value";
    strategy.put(null, value);
    assertEquals(value, strategy.get(null));
  }
}
