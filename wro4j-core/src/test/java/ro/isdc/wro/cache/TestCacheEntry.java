/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.cache;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * {@link CacheEntry} test class
 *
 * @author Alex Objelean
 */
public class TestCacheEntry {

  @Test
  public void testEquals() {
    final CacheEntry key1 = new CacheEntry("g1", ResourceType.JS, true);
    final CacheEntry key2 = new CacheEntry("g1", ResourceType.JS, true);
    Assert.assertEquals(key1, key2);
  }

  @Test
  public void testEquals2() {
    final CacheEntry key1 = new CacheEntry("g1", ResourceType.JS, false);
    final CacheEntry key2 = new CacheEntry("g1", ResourceType.JS, true);
    Assert.assertNotSame(key1, key2);
  }
}
