package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.http.support.DelegatingServletInputStream;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.util.io.NullOutputStream;

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
  public void setUp()
      throws Exception {
    victim = new GzipFilter();
    MockitoAnnotations.initMocks(this);
    when(mockRequest.getInputStream()).thenReturn(
        new DelegatingServletInputStream(new ByteArrayInputStream("".getBytes())));
    when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new NullOutputStream()));
    // mock chaining
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        final ServletRequest request = (ServletRequest) invocation.getArguments()[0];
        final ServletResponse response = (ServletResponse) invocation.getArguments()[1];
        IOUtils.copy(request.getInputStream(), response.getOutputStream());
        return null;
      }
    }).when(mockFilterChain).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
  }

  @Test
  public void shouldChainWhenGzipIsNotSupported() throws Exception {
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockFilterChain, Mockito.times(1)).doFilter(mockRequest, mockResponse);
  }


  @Test
  public void shouldGzipWhenGzipSupported() throws Exception {
    markGzipAsAllowed();
    final String content = "sampleContent";
    final InputStream sourceStream = new ByteArrayInputStream(content.getBytes());
    final ByteArrayOutputStream targetStream = new ByteArrayOutputStream();


    when(mockRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(sourceStream));
    when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(targetStream));

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);

    //not the same response is chained
    verify(mockFilterChain, Mockito.never()).doFilter(mockRequest, mockResponse);
    //chain is invoked but with a different response
    verify(mockFilterChain, Mockito.times(1)).doFilter(Mockito.eq(mockRequest), Mockito.any(HttpServletResponse.class));

    final InputStream ungzippedStream = new GZIPInputStream(new ByteArrayInputStream(targetStream.toByteArray()));

    assertEquals(content, IOUtils.toString(ungzippedStream));
  }

  private void markGzipAsAllowed() {
    final String headerName = "Accept-Encoding";
    when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(headerName)));
    when(mockRequest.getHeader(headerName)).thenReturn("gzip");
  }
}
