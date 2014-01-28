package ro.isdc.wro.http.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * Test the behavior of {@link ResponseHeadersConfigurer}
 * 
 * @author Alex Objelean
 */
public class TestResponseHeadersConfigurer {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain chain;
  private ResponseHeadersConfigurer victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp()
      throws Exception {
    MockitoAnnotations.initMocks(this);
    victim = new ResponseHeadersConfigurer();
  }
  
  @Test
  public void shouldHaveNoConfiguredHeadersWhenDefaultHeadersAreNotSet() {
    victim = ResponseHeadersConfigurer.emptyHeaders();
    assertTrue(victim.getHeadersMap().entrySet().isEmpty());
  }
  
  @Test
  public void shouldUseConfiguredDefaultHeaders() {
    final String etag = "123";
    victim = new ResponseHeadersConfigurer() {
      @Override
      public void configureDefaultHeaders(final Map<String, String> map) {
        map.put(HttpHeader.ETAG.name(), etag);
      }
    };
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(1, map.size());
    assertEquals(etag, map.get(HttpHeader.ETAG.getHeaderName()));
  }
  
  @Test
  public void shouldHaveNoCacheHeadersInDebugMode() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(true);
    victim = ResponseHeadersConfigurer.fromConfig(config);
    
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(3, map.size());
    assertEquals("no-cache", map.get(HttpHeader.PRAGMA.getHeaderName()));
    assertEquals("no-cache", map.get(HttpHeader.CACHE_CONTROL.getHeaderName()));
    assertEquals("0", map.get(HttpHeader.EXPIRES.getHeaderName()));
  }
  
  @Test
  public void shouldSetCacheControlForOneYearWhenDebugModeIsFalse() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(false);
    victim = ResponseHeadersConfigurer.fromConfig(config);
    
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(3, map.size());
    assertEquals("public, max-age=315360000", map.get(HttpHeader.CACHE_CONTROL.getHeaderName()));
  }
  
  @Test
  public void shouldUseHeadersSetAsString() {
    victim = ResponseHeadersConfigurer.withHeadersSet("h1:v1 | h2:v2");
    
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals("v1", map.get("h1"));
    assertEquals("v2", map.get("h2"));
    assertEquals(2, map.size());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseHeadersSetWronglyAsString() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(true);
    config.setHeader("h1=v1 , h2 =v2");
    victim = ResponseHeadersConfigurer.fromConfig(config);
    
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(2, map.size());
    assertEquals("v1", map.get("h1"));
    assertEquals("v2", map.get("h2"));
  }
}
