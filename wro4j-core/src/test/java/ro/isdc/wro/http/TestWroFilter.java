/**
 *
 */
package ro.isdc.wro.http;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * TestWroFilter.java.
 *
 * @author alexandru.objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroFilter {
  @Test
  public void testGzipParam() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterChain chain = Mockito.mock(FilterChain.class);
    //new WroFilter().doFilter(request, response, chain);
  }
}
