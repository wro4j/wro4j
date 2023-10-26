package ro.isdc.wro.extensions.http.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class TestModelAsJsonRequestHandler {
  private ModelAsJsonRequestHandler victim;
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
    initMocks(this);
    Context.set(Context.webContext(mockRequest, mockResponse, mock(FilterConfig.class)));

    victim = new ModelAsJsonRequestHandler();

    final WroModel wroModel = createSimpleModelStub();

    when(mockModelFactory.create()).thenReturn(wroModel);
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(mockModelFactory);
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);

    // Setup response writer
    outputStream = new ByteArrayOutputStream();
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
    final PrintWriter printWriter = new PrintWriter(writer);
    when(mockResponse.getWriter()).thenReturn(printWriter);

    when(mockRequest.getRequestURI()).thenReturn("/wro/wroApi/model");
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldBeEnabledByDefault() {
    assertTrue(victim.isEnabled());
  }

  @Test
  public void shouldAcceptRequestsWithCorrectURI() {
    when(mockRequest.getRequestURI()).thenReturn(ModelAsJsonRequestHandler.ENDPOINT_URI);
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
    ObjectMapper mapper = new ObjectMapper(); 
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true); 
    ObjectNode expected = mapper.readValue(readJsonFile("wroModel_simple.json"), ObjectNode.class); 
    ObjectNode actual = mapper.readValue(outputStream.toString(), ObjectNode.class); 
    assertEquals(expected, actual); 
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
    final boolean accept = victim.accept(mockRequest);
    assertTrue(accept);
  }

  @Test
  public void shouldNotProvideProxyUriForExternalResources()
      throws IOException {
    when(mockModelFactory.create()).thenReturn(createWroModelExternalModelStub());
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(mockModelFactory);
    victim = new ModelAsJsonRequestHandler();
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);

    victim.handle(mockRequest, mockResponse);
    assertEquals(readJsonFile("wroModel_external.json"), outputStream.toString());
  }

  private WroModel createWroModelExternalModelStub() {
    final WroModel wroModel = new WroModel();
    final List<Group> wroGroups = new ArrayList<Group>();
    wroGroups.addAll(createSimpleModelStub().getGroups());

    final Group extGroup = new Group("external");
    final Resource resource = new Resource();
    resource.setType(ResourceType.JS);
    resource.setUri("https://www.site.com/style.js");
    resource.setMinimize(false);

    extGroup.addResource(resource);
    extGroup.addResource(Resource.create("http://www.site.com/style.css"));
    wroGroups.add(extGroup);

    wroModel.setGroups(wroGroups);
    return wroModel;
  }

  private WroModel createSimpleModelStub() {
    final WroModel wroModel = new WroModel();
    final List<Group> wroGroups = new ArrayList<Group>();
    final Group group = new Group("test");
    final Resource resource = new Resource();
    resource.setType(ResourceType.JS);
    resource.setUri("test.js");
    resource.setMinimize(true);
    group.addResource(resource);
    wroGroups.add(group);
    wroModel.setGroups(wroGroups);
    return wroModel;
  }

  private String readJsonFile(final String filename)
      throws IOException {
    final String packagePath = WroUtil.toPackageAsFolder(this.getClass());
    final InputStream is = this.getClass().getClassLoader().getResourceAsStream(packagePath + "/" + filename);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    final StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      sb.append(line + "\n");
    }
    is.close();
    return sb.toString().trim();
  }
}
