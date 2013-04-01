package ro.isdc.wro.http.handler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
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
  private ResourceLocatorFactory mockLocatorFactory;
  @Mock
  private ResourceLocator mockLocator;

  private OutputStream outputStream;

  private ServletOutputStream servletOutputStream;

  private String packagePath;

  @Before
  public void setup()
      throws IOException {
    MockitoAnnotations.initMocks(this);
    victim = new ResourceProxyRequestHandler();

    Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
    Context.set(Context.webContext(request, response, filterConfig));
    // a more elaborate way to build injector, used to instruct it use a different instance of authorizationManager
    final Injector injector = new InjectorBuilder(
        new BaseWroManagerFactory().setLocatorFactory(mockLocatorFactory).setResourceAuthorizationManager(
        mockAuthorizationManager)).build();
    injector.inject(victim);

    when(mockLocatorFactory.getLocator(anyString())).thenReturn(mockLocator);
    when(mockLocatorFactory.locate(anyString())).thenAnswer(new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        return mockLocator.getInputStream();
      }
    });
    when(mockLocator.getInputStream()).thenReturn(WroUtil.EMPTY_STREAM);

    packagePath = WroUtil.toPackageAsFolder(this.getClass());

    // Setup response writer
    outputStream = new ByteArrayOutputStream();
    servletOutputStream = new ServletOutputStream() {
      @Override
      public void write(final int i)
          throws IOException {
        outputStream.write(i);
      }
    };
    when(response.getOutputStream()).thenReturn(servletOutputStream);

  }

  @Test
  public void shouldAlwaysBeEnabled() {
    assertTrue(victim.isEnabled());
  }

  @Test
  public void shouldAcceptCallsTo_wroResources() {
    when(request.getRequestURI()).thenReturn("/wro/wroResources?id=test.css");
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
    when(mockLocator.getInputStream()).thenReturn(new ClasspathResourceLocator(resourceUri).getInputStream());

    victim.handle(request, response);

    final String body = outputStream.toString();
    final String expectedBody = IOUtils.toString(getInputStream("test.css"));

    assertEquals(expectedBody, body);
  }

  @Test(expected = UnauthorizedRequestException.class)
  public void cannotProxyUnauthorizedResources()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(false);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);
    when(mockLocator.getInputStream()).thenReturn(new ClasspathResourceLocator(resourceUri).getInputStream());

    victim.handle(request, response);
  }

  @Test
  public void shouldReturnRelativeResource()
      throws IOException {
    final String resourceUri = "/" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);

    // Set up victim
    when(mockLocator.getInputStream()).thenReturn(getInputStream("test.css"));
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    // Perform Action
    victim.handle(request, response);
    final String body = outputStream.toString();
    final String expectedBody = IOUtils.toString(getInputStream("test.css"));

    verify(mockLocator, times(1)).getInputStream();
    assertEquals(expectedBody, body);
  }

  @Test
  public void shouldSetResponseLength()
      throws IOException {
    final String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(mockAuthorizationManager.isAuthorized(resourceUri)).thenReturn(true);
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);
    when(mockLocator.getInputStream()).thenReturn(new ClasspathResourceLocator(resourceUri).getInputStream());

    victim.handle(request, response);
    final int expectedLength = IOUtils.toString(getInputStream("test.css")).length();

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

  private InputStream getInputStream(final String filename)
      throws IOException {
    return this.getClass().getClassLoader().getResourceAsStream(packagePath + "/" + filename);
  }
}