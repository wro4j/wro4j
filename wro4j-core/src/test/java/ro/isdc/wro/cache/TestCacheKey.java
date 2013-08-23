/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * {@link CacheKey} test class
 *
 * @author Alex Objelean
 */
public class TestCacheKey {

  @Test
  public void testEquals() {
    final CacheKey key1 = new CacheKey("g1", ResourceType.JS, true);
    final CacheKey key2 = new CacheKey("g1", ResourceType.JS, true);
    assertEquals(key1, key2);
  }

  @Test
  public void testEquals2() {
    final CacheKey key1 = new CacheKey("g1", ResourceType.JS, false);
    final CacheKey key2 = new CacheKey("g1", ResourceType.JS, true);
    assertFalse(key1.equals(key2));
  }

  private CacheKey createValidCacheKey() {
    return new CacheKey("g1", ResourceType.JS, false);
  }

  @Test
  public void addingNullAttributeKeyShouldHaveNoEffect() {
    final CacheKey key = createValidCacheKey().addAttribute(null, "");
    assertEquals(key, createValidCacheKey());
  }

  @Test
  public void addingNullAttributeValueShouldHaveNoEffect() {
    final CacheKey key = createValidCacheKey().addAttribute("key1", null);
    assertEquals(key, createValidCacheKey());
  }

  @Test
  public void shouldBeSameWhenContainingIdenticalAttributes() {
    final CacheKey key1 = createValidCacheKey().addAttribute("k1", "v1");
    final CacheKey key2 = createValidCacheKey().addAttribute("k1", "v1");
    assertEquals(key1, key2);
    assertEquals(key1.hashCode(), key2.hashCode());
  }

  @Test
  public void shouldDifferWhenContainingDifferentAttributes() {
    final CacheKey key1 = createValidCacheKey().addAttribute("k1", "v1").addAttribute("k2", "v2");
    final CacheKey key2 = createValidCacheKey().addAttribute("k1", "v1");
    assertFalse(key1.equals(key2));
    assertFalse(key1.hashCode() == key2.hashCode());
  }
}
