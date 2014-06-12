package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.http.handler.ModelAsJsonRequestHandler;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test behavior of {@link WroFilter} when the extensions module is available.
 *
 * @author Alex Objelean
 */
public class TestWroFilter {
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterChain mockFilterChain;
  private WroFilter victim;

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
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    victim = new WroFilter();
    victim.setWroManagerFactory(new BaseWroManagerFactory().setModelFactory(WroTestUtils.simpleModelFactory(new WroModel())));
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldInvokeModelAsJsonRequestHandler()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn(ModelAsJsonRequestHandler.ENDPOINT_URI);
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    Mockito.verify(mockFilterChain, Mockito.never()).doFilter(Mockito.any(ServletRequest.class),
        Mockito.any(ServletResponse.class));
  }
}
