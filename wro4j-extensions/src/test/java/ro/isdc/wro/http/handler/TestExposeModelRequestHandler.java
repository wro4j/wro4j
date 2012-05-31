package ro.isdc.wro.http.handler;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.http.handler.ExposeModelRequestHandler;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;


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
    Context.set(Context.standaloneContext());
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
  
  @Test
  public void shouldGenerateModelAsJson()
      throws IOException, ServletException {
    victim.handle(mockRequest, mockResponse);
    final String actual = outputStream.toString();
    
    assertEquals(
        "{\"groups\":[{\"name\":\"test\",\"resources\":[{\"type\":\"JS\",\"uri\":\"test.js\",\"minimize\":true}]}]}",
        actual);
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
  public void shouldSetCorrectContentType()
      throws IOException, ServletException {
    victim.handle(mockRequest, mockResponse);
    verify(mockResponse, times(1)).setContentType(ExposeModelRequestHandler.ENDPOINT_URI);
  }
}