package ro.isdc.wro.extensions.http.handler;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Ivar Conradi Østhus
 * @author Alex Objelean
 */
public class TestExposeModelRequestHandler {
  private ExposeModelRequestHandler victim;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private WroModelFactory mockModelFactory;
  
  private OutputStream outputStream;
  
  @Before
  public void setUp()
      throws Exception {
    Context.set(Context.webContext(mockRequest, mockResponse, mock(FilterConfig.class)));
    
    MockitoAnnotations.initMocks(this);
    
    victim = new ExposeModelRequestHandler();
    
    final WroModel wroModel = createSimpleModelStub();
    
    when(mockModelFactory.create()).thenReturn(wroModel);
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(mockModelFactory);
    Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);
    
    // Setup response writer
    outputStream = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
    PrintWriter printWriter = new PrintWriter(writer);
    when(mockResponse.getWriter()).thenReturn(printWriter);

    when(mockRequest.getRequestURI()).thenReturn("/wro/wroApi/model");
  }
  
  @Test
  public void shouldBeEnabledByDefault() {
    Assert.assertTrue(victim.isEnabled());
  }
  
  @Test
  public void shouldAcceptRequestsWithCorrectURI() {
    when(mockRequest.getRequestURI()).thenReturn(ExposeModelRequestHandler.ENDPOINT_URI);
    assertTrue(victim.accept(mockRequest));
  }
  
  @Test
  public void shouldNotAcceptRequestsWithWrongURI() {
    when(mockRequest.getRequestURI()).thenReturn("/path/to/anotherURI");
    assertFalse(victim.accept(mockRequest));
  }
  
  @Test
  public void shouldGenerateModelAsJson()
      throws Exception {
    victim.handle(mockRequest, mockResponse);
    String body = outputStream.toString();

    assertThat(body, is(readJsonFile("wroModel_simple.json")));
  }

  @Test
  public void shouldSetCorrectContentType()
      throws IOException, ServletException {
    when(mockRequest.getRequestURI()).thenReturn("wroAPI/model");
    victim.handle(mockRequest, mockResponse);
    verify(mockResponse, times(1)).setContentType("application/json");
    verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  public void shouldAcceptUri() {
    when(mockRequest.getRequestURI()).thenReturn("wroApi/model");
    boolean accept = victim.accept(mockRequest);
    assertThat(accept, is(true));
  }
  
  @Test
  public void shouldNotProvideProxyUriForExternalResources() throws IOException {
    when(mockModelFactory.create()).thenReturn(createWroModelExternalModelStub());
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(mockModelFactory);
    victim = new ExposeModelRequestHandler();
    Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);

    victim.handle(mockRequest, mockResponse);
    String body = outputStream.toString();

    assertThat(body, is(readJsonFile("wroModel_external.json")));

  }

  private WroModel createWroModelExternalModelStub() {
    WroModel wroModel = new WroModel();
    List<Group> wroGroups = new ArrayList<Group>();
    wroGroups.addAll(createSimpleModelStub().getGroups());

    Group extGroup = new Group("external");
    Resource resource = new Resource();
    resource.setType(ResourceType.CSS);
    resource.setUri("http://www.site.com/style.css");
    resource.setMinimize(true);
    extGroup.addResource(resource);
    wroGroups.add(extGroup);

    wroModel.setGroups(wroGroups);
    return wroModel;
  }

  private WroModel createSimpleModelStub() {
    WroModel wroModel = new WroModel();
    List<Group> wroGroups = new ArrayList<Group>();
    Group group = new Group("test");
    Resource resource = new Resource();
    resource.setType(ResourceType.JS);
    resource.setUri("test.js");
    resource.setMinimize(true);
    group.addResource(resource);
    wroGroups.add(group);
    wroModel.setGroups(wroGroups);
    return wroModel;
  }

  private String readJsonFile(String filename)
      throws IOException {
    String packagePath = WroUtil.toPackageAsFolder(this.getClass());
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(packagePath + "/" + filename);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      sb.append(line + "\n");
    }
    is.close();
    return sb.toString().trim();
  }
}