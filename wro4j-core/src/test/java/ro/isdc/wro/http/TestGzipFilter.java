package ro.isdc.wro.http;

import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Alex Objelean
 */
public class TestGzipFilter {
  private GzipFilter victim;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterChain mockFilterChain;


  @Before
  public void setUp() {
    victim = new GzipFilter();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldChainWhenGzipIsNotSupported() throws Exception {
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockFilterChain, Mockito.times(1)).doFilter(mockRequest, mockResponse);
  }


  @Test
  public void shouldGzipWhenGzipSupported() throws Exception {
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockFilterChain, Mockito.times(1)).doFilter(mockRequest, mockResponse);
  }
}
