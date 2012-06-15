package ro.isdc.wro.extensions.http.handler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

public class TestWroModelAsJsonRequestHandler {
  
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpServletRequest request;
  @Mock
  private WroManagerFactory wroManagerFactory;
  @Mock
  private WroModelFactory wroModelFactory;
  
  private OutputStream outputStream;
  
  private WroManager wroManager;
  
  private WroModel wroModel;
  
  private WroModelAsJsonRequestHandler victim;
  
  @Before
  public void setUp()
      throws Exception {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.webContext(request, response, mock(FilterConfig.class)));

    wroModel = createSimpleModelStub();
    wroManager = new WroManager();
    wroManager.setModelFactory(wroModelFactory);

    when(wroManagerFactory.create()).thenReturn(wroManager);
    when(wroModelFactory.create()).thenReturn(wroModel);

    Injector injector = InjectorBuilder.create(wroManagerFactory).build();
    victim = new WroModelAsJsonRequestHandler();
    injector.inject(victim);

    // Setup response writer
    outputStream = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
    PrintWriter printWriter = new PrintWriter(writer);
    when(response.getWriter()).thenReturn(printWriter);
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
    when(request.getRequestURI()).thenReturn("wroApi/model");
    victim.handle(request, response);
    String body = outputStream.toString();
    
    assertThat(
        body,
        is("{\n  \"groups\": [\n    {\n      \"name\": \"test\",\n      \"resources\": [\n        {\n          \"type\": \"JS\",\n          \"internalUri\": \"test.js\",\n          \"externalUri\": \"wroResources?id=test.js\"\n        }\n      ]\n    }\n  ]\n}"));
  }

  @Test
  public void shouldSetCorrectContentType()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("wroApi/model");
    victim.handle(request, response);
    verify(response, times(1)).setContentType("application/json");
    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  public void shouldAcceptUri() {
    when(request.getRequestURI()).thenReturn("wroApi/model");
    boolean accept = victim.accept(request);
    assertThat(accept, is(true));
  }
}
