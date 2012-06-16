package ro.isdc.wro.http.handler;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ivar Conradi Østhus
 */
public class TestResourceProxyRequestHandler {
  private ResourceProxyRequestHandler victim;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  private OutputStream outputStream;

  private ServletOutputStream servletOutputStream;

  private String packagePath;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    victim = new ResourceProxyRequestHandler();

    Context.set(Context.webContext(request, response, mock(FilterConfig.class)));

    WroTestUtils.createInjector().inject(victim);

    packagePath = WroUtil.toPackageAsFolder(this.getClass());

    // Setup response writer
    outputStream = new ByteArrayOutputStream();
    servletOutputStream = new ServletOutputStream() {
      @Override
      public void write(int i) throws IOException {
        outputStream.write(i);
      }
    };
    when(response.getOutputStream()).thenReturn(servletOutputStream);

  }

  @Test
  public void shouldAlwaysBeEnabled() {
    assertThat(victim.isEnabled(), is(true));
  }

  @Test
  public void shouldAcceptCallsTo_wroResources() {
    when(request.getRequestURI()).thenReturn("/wro/wroResources?id=test.css");
    assertThat(victim.accept(request), is(true));
  }

  @Test
  public void shouldNotAcceptCallsTo_OtherUris() {
    when(request.getRequestURI()).thenReturn("/wro/someOtherUri");
    assertThat(victim.accept(request), is(false));
  }

  @Test
  public void shouldReturnClasspathResource() throws IOException {
    String resourceUri = "classpath:" + packagePath + "/" + "test.css";
    when(request.getParameter(ResourceProxyRequestHandler.PARAM_RESOURCE_ID)).thenReturn(resourceUri);

    victim.handle(request, response);

    String body = outputStream.toString();
    String expectedBody = loadTestResource("test.css");

    assertThat(body, is(expectedBody));
  }

  private String loadTestResource(String filename) throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(packagePath + "/" + filename);
    return IOUtils.toString(is);
  }
}
