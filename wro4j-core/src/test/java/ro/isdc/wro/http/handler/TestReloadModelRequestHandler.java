package ro.isdc.wro.http.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;

public class TestReloadModelRequestHandler {

  private RequestHandler reloadModelRequestHandler;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Before
  public void setup() {
    reloadModelRequestHandler = new ReloadModelRequestHandler();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);

    Context.set(Context.webContext(request, response, mock(FilterConfig.class)));
  }

  @Test
  public void shouldHandleRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/reloadModel");
    assertTrue(reloadModelRequestHandler.accept(request));
  }

  @Test
  public void shouldNotHandleRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/somethingElse");
    assertFalse(reloadModelRequestHandler.accept(request));
  }

  @Test
  public void shouldReloadCache() throws IOException, ServletException {
    reloadModelRequestHandler.handle(request, response);
    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
  }
}
