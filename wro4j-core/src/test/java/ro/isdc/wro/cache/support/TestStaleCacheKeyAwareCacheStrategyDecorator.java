package ro.isdc.wro.cache.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * @author Alex Objelean
 */
public class TestStaleCacheKeyAwareCacheStrategyDecorator {
  private static final String DEFAULT_KEY = "keyDefault";
  @Mock
  private CacheStrategy<String, String> decorated;
  private StaleCacheKeyAwareCacheStrategyDecorator<String, String> victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new StaleCacheKeyAwareCacheStrategyDecorator<String, String>(decorated);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotMarkAsStaleNullKey() {
    victim.markAsStale(null);
  }
  
  @Test
  public void shouldNotBeStaleWhenInvokedFirstTime() {
    assertFalse(victim.isStale(DEFAULT_KEY));
  }
  

  @Test
  public void shouldBeStaleAfterMarkedAsStale() {
    victim.markAsStale(DEFAULT_KEY);
    assertTrue(victim.isStale(DEFAULT_KEY));
  }
  
  @Test
  public void shouldNotBeStaleAfterMarkedAsStaleAndUpdated() {
    victim.markAsStale(DEFAULT_KEY);
    victim.put(DEFAULT_KEY, "someValue");
    assertFalse(victim.isStale(DEFAULT_KEY));
  }
  
  @Test
  public void shouldNotBeStaleAfterMarkedAsStaleAndUpdatedWithNull() {
    victim.markAsStale(DEFAULT_KEY);
    victim.put(DEFAULT_KEY, null);
    assertFalse(victim.isStale(DEFAULT_KEY));
  }
  
  @Test
  public void shouldReturnCachedStaleValueAfterKeyMarkedAsStale() {
    final String value = "DefaultValue";
    Mockito.when(decorated.get(Mockito.eq(DEFAULT_KEY))).thenReturn(value);
    victim.markAsStale(DEFAULT_KEY);
    Mockito.verify(decorated).get(Mockito.eq(DEFAULT_KEY));
    Mockito.reset(decorated);
    assertTrue(victim.isStale(DEFAULT_KEY));
    assertEquals(value, victim.get(DEFAULT_KEY));
    Mockito.verify(decorated, Mockito.never()).get(Mockito.eq(DEFAULT_KEY));
  }
  
  @Test
  public void shouldHaveNoStaleKeyAfterDestroy() {
    victim.markAsStale(DEFAULT_KEY);
    assertTrue(victim.isStale(DEFAULT_KEY));
    victim.destroy();
    assertFalse(victim.isStale(DEFAULT_KEY));
  }
}
