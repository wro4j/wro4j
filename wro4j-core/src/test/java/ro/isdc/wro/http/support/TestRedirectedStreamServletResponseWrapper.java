package ro.isdc.wro.http.support;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * @author Alex Objelean
 */
public class TestRedirectedStreamServletResponseWrapper {
  private RedirectedStreamServletResponseWrapper victim;
  @Mock
  private HttpServletResponse mockResponse;
  private ByteArrayOutputStream redirectedStream;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    redirectedStream = new ByteArrayOutputStream();
    MockitoAnnotations.initMocks(this);
    victim = new RedirectedStreamServletResponseWrapper(redirectedStream, mockResponse);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptNullResponse() {
    new RedirectedStreamServletResponseWrapper(new ByteArrayOutputStream(), null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullStream() {
    new RedirectedStreamServletResponseWrapper(null, mockResponse);
  }

  @Test
  public void shouldRedirectWriter() throws Exception {
    final String message = "Hello world!";
    victim.getWriter().write(message);
    victim.getWriter().flush();
    Assert.assertEquals(message, new String(redirectedStream.toByteArray()));
  }

  @Test
  public void shouldRedirectStream() throws Exception {
    final String message = "Hello world!";
    victim.getOutputStream().write(message.getBytes());
    victim.getOutputStream().flush();
    Assert.assertEquals(message, new String(redirectedStream.toByteArray()));
  }

  /**
   * instruct vitim to use custom external resource locator (to return expected message).
   */
  @Test
  public void shouldRedirectStreamWhenSendRedirectIsInvoked() throws Exception {
    final String message = "Hello world!";
    victim = new RedirectedStreamServletResponseWrapper(redirectedStream, mockResponse) {
      @Override
      protected UriLocator newExternalResourceLocator() {
        return new ClasspathUriLocator() {
          @Override
          public InputStream locate(final String uri)
              throws IOException {
            return new ByteArrayInputStream(message.getBytes());
          }
        };
      }
    };
    victim.sendRedirect("/does/not/matter");
    assertEquals(message, new String(redirectedStream.toByteArray()));
  }
}
