package ro.isdc.wro.http.support;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;


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
    victim.setDebug(true);
    victim.reset();
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(3, map.size());
    assertEquals("no-cache", map.get(HttpHeader.PRAGMA.getHeaderName()));
    assertEquals("no-cache", map.get(HttpHeader.CACHE_CONTROL.getHeaderName()));
    assertEquals("0", map.get(HttpHeader.EXPIRES.getHeaderName()));
  }
  
  @Test
  public void shouldSetCacheControlForOneYearWhenDebugModeIsFalse() {
    victim.setDebug(false);
    victim.reset();
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(3, map.size());
    assertEquals("public, max-age=315360000", map.get(HttpHeader.CACHE_CONTROL.getHeaderName()));
  }
  
  @Test
  public void shouldUseHeadersSetAsString() {
    victim = ResponseHeadersConfigurer.emptyHeaders();
    victim.setHeaders("h1:v1 | h2:v2");
    victim.reset();
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(2, map.size());
    assertEquals("v1", map.get("h1"));
    assertEquals("v2", map.get("h2"));
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotUseHeadersSetWronglyAsString() {
    victim = ResponseHeadersConfigurer.emptyHeaders();
    victim.setHeaders("h1=v1 , h2 =v2");
    victim.reset();
    final Map<String, String> map = victim.getHeadersMap();
    assertEquals(2, map.size());
    assertEquals("v1", map.get("h1"));
    assertEquals("v2", map.get("h2"));
  }
}
