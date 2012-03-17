package ro.isdc.wro.http.support;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Alex Objelean
 */
public class TestRedirectedStreamServletResponseWrapper {
  private RedirectedStreamServletResponseWrapper victim;
  @Mock
  private HttpServletResponse mockResponse;
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new RedirectedStreamServletResponseWrapper(new ByteArrayOutputStream(), mockResponse);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptNullResponse() {
    new RedirectedStreamServletResponseWrapper(new ByteArrayOutputStream(), null);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullStream() {
    new RedirectedStreamServletResponseWrapper(null, mockResponse);
  }
}
