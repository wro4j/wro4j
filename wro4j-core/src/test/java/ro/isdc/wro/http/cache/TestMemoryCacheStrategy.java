/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.http.cache;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test class for MapCacheStrategy.
 *
 * @author Alex Objelean
 */
public class TestMemoryCacheStrategy {
  private CacheStrategy<CacheEntry, String> strategy;
  @Before
  public void setUp() {
    strategy = new MemoryCacheStrategy<CacheEntry, String>();
  }

  @Test
  public void testPut() {
    final CacheEntry key = new CacheEntry("g1", ResourceType.JS, true);
    final String value = "value";
    strategy.put(key, value);
    Assert.assertEquals(value, strategy.get(key));
  }
}
