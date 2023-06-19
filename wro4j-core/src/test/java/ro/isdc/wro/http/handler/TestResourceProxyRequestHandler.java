package ro.isdc.wro.http.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Ivar Conradi Østhus
 */
public class TestResourceProxyRequestHandler {
  @InjectMocks
  private ResourceProxyRequestHandler victim;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private ServletContext servletContext;
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private ResourceAuthorizationManager mockAuthorizationManager;
  @Mock
  private UriLocatorFactory mockUriLocatorFactory;
  @Mock
  private UriLocator mockUriLocator;

  private OutputStream outputStream;

  private ServletOutputStream servletOutputStream;

  private String packagePath;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

	@Before
	public void setup() throws IOException {

		MockitoAnnotations.initMocks(this);
		victim = new ResourceProxyRequestHandler();

		Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
		Context.set(Context.webContext(request, response, filterConfig));
		// a more elaborate way to build injector, used to instruct it use a different
		// instance of authorizationManager
		final Injector injector = new InjectorBuilder(new BaseWroManagerFactory()
				.setUriLocatorFactory(mockUriLocatorFactory).setResourceAuthorizationManager(mockAuthorizationManager))
						.build();
		injector.inject(victim);

		when(mockUriLocatorFactory.getInstance(anyString())).thenReturn(mockUriLocator);
		when(mockUriLocatorFactory.locate(anyString())).then(new Answer<InputStream>() {
			public InputStream answer(final InvocationOnMock invocation) throws Throwable {
				final String uri = (String) invocation.getArguments()[0];
				return mockUriLocator.locate(uri);
			}
		});
		when(mockUriLocator.locate(anyString())).thenReturn(WroUtil.EMPTY_STREAM);

		packagePath = WroUtil.toPackageAsFolder(this.getClass());

		// Setup response writer
		outputStream = new ByteArrayOutputStream();
		servletOutputStream = new ServletOutputStream() {
			@Override
			public void write(final int i) throws IOException {
				outputStream.write(i);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				// Nothing to do.
			}
		};
		when(response.getOutputStream()).thenReturn(servletOutputStream);
	}

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldAlwaysBeEnabled() {
    assertTrue(victim.isEnabled());
  }

  @Test
  public void shouldAcceptCallsTo_wroResources() {
    when(request.getRequestURI()).thenReturn("?wroAPI=wroResources&id=test.css");
    when(request.getParameter(ResourceProxyRequestHandler.PATH_API)).thenReturn(
        ResourceProxyRequestHandler.PATH_RESOURCES);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn("test.css");
    assertTrue(victim.accept(request));
  }

  @Test
  public void shouldNotAcceptCallsTo_OtherUris() {
    when(request.getRequestURI()).thenReturn("/wro/someOtherUri");
    assertFalse(victim.accept(request));
  }

  @Test
  public void shouldReturnClasspathResource()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);
    when(mockUriLocator.locate(anyString())).thenReturn(new ClasspathUriLocator().locate(resourceUri));

    victim.handle(request, response);

    final String body = outputStream.toString();
    final String expectedBody = IOUtils.toString(getInputStream("test.css"), Charset.defaultCharset());

    assertEquals(expectedBody, body);
  }

  @Test(expected = UnauthorizedRequestException.class)
  public void cannotProxyUnauthorizedResources()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(false);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);
    when(mockUriLocator.locate(anyString())).thenReturn(new ClasspathUriLocator().locate(resourceUri));

    victim.handle(request, response);
  }

  @Test
  public void shouldReturnRelativeResource()
      throws IOException {
    final String resourceUri = "/" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);

    // Set up victim
    when(mockUriLocator.locate(resourceUri)).thenReturn(getInputStream("test.css"));
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    // Perform Action
    victim.handle(request, response);
    final String body = outputStream.toString();
    final String expectedBody = IOUtils.toString(getInputStream("test.css"), Charset.defaultCharset());

    verify(mockUriLocator, times(1)).locate(resourceUri);
    assertEquals(expectedBody, body);
  }

  @Test
  public void shouldSetResponseLength()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);
    when(mockUriLocator.locate(anyString())).thenReturn(new ClasspathUriLocator().locate(resourceUri));

    victim.handle(request, response);
    final int expectedLength = IOUtils.toString(getInputStream("test.css"), Charset.defaultCharset()).length();

    verify(response, times(1)).setContentLength(expectedLength);
  }

  @Test
  public void shouldSetCSSContentType()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    victim.handle(request, response);

    verify(response, times(1)).setContentType("text/css; charset=UTF-8");
  }

  @Test
  public void shouldSetJSContentType()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.js";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    victim.handle(request, response);

    verify(response, times(1)).setContentType("application/javascript; charset=UTF-8");
  }

  @Test
  public void shouldSetPNGContentType()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.png";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    victim.handle(request, response);

    verify(response, times(1)).setContentType("image/png");
  }

  @Test
  public void shouldSetCorrectResopnsCodeBasedOnResourceChangeState()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    final long timeInFuture = new Date().getTime() + 10000;
    when(request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString())).thenReturn(timeInFuture);
    victim.handle(request, response);
    verify(response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);

    final long longTimeAgo = 123L;
    when(request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString())).thenReturn(longTimeAgo);
    victim.handle(request, response);
    verify(response).setStatus(Mockito.eq(HttpServletResponse.SC_OK));
  }

  @Test
  public void shouldAssumeResourceChangedWhenModifiedSinceHeaderExtractionFails()
      throws Exception {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    when(request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString())).thenThrow(
        new IllegalArgumentException("BOOM!"));
    victim.handle(request, response);
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  private InputStream getInputStream(final String filename)
      throws IOException {
    return this.getClass().getClassLoader().getResourceAsStream(packagePath + "/" + filename);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullRequestUriToCreateProxyPath() {
    ResourceProxyRequestHandler.createProxyPath(null, "");
  }

  @Test
  public void shouldCreateProxyPath() {
    assertEquals("/wro/all.js?wroAPI=wroResources&id=id", ResourceProxyRequestHandler.createProxyPath("/wro/all.js", "id"));
    assertEquals("?wroAPI=wroResources&id=id", ResourceProxyRequestHandler.createProxyPath("", "id"));
  }

  @Test
  public void shouldDetectProxyUri() {
    assertTrue(ResourceProxyRequestHandler.isProxyUri("/path?wroAPI=wroResources&id=id"));
  }
}
