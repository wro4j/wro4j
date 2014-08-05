/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test class for MapCacheStrategy.
 *
 * @author Alex Objelean
 */
public class TestMemoryCacheStrategy {
  private CacheStrategy<CacheKey, String> strategy;
  private HashMap<CacheKey, String> map;
  @Before
  public void setUp() {
    map = Mockito.spy(new LinkedHashMap<CacheKey, String>());
    strategy = new MemoryCacheStrategy<CacheKey, String>(map);
  }

  @Test
  public void testPut() {
    final CacheKey key = createKey();
    final String value = "value";
    strategy.put(key, value);
    assertEquals(value, strategy.get(key));
  }

  @Test
  public void shouldNotContainMultipleVersionsOfSameKey() {
    final String value = "value";
    strategy.put(createKey(), value);
    strategy.put(createKey(), value);
    strategy.put(createKey(), value);
    strategy.put(createKey(), value);
    assertTrue(map.size() == 1);
  }

  private CacheKey createKey() {
    return new CacheKey("g1", ResourceType.JS, true);
  }
}
