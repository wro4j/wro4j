package ro.isdc.wro.http.support;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.isdc.wro.http.support.WroModelToJsonHelper;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import javax.servlet.*;
  import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TestWroModelToJsonHelper {
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private WroManagerFactory wroManagerFactory;
  @Mock
  private WroModelFactory wroModelFactory;

  private OutputStream outputStream;

  private WroManager wroManager;

  private WroModel wroModel;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    wroModel = createSimpleModelStub();

    wroManager = new WroManager();
    wroManager.setModelFactory(wroModelFactory);

    when(wroManagerFactory.create()).thenReturn(wroManager);
    when(wroModelFactory.create()).thenReturn(wroModel);

    //Setup response writer
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
  public void shouldGenerateModelAsJson() throws IOException, ServletException {
    WroModelToJsonHelper.produceJson(mockResponse, wroManagerFactory);
    String body = outputStream.toString();

    assertThat(body, is("{\"groups\":[{\"name\":\"test\",\"resources\":[{\"type\":\"JS\",\"uri\":\"test.js\",\"minimize\":true}]}]}"));
  }

  @Test
  public void shouldSetCorrectContentType() throws IOException, ServletException {
    WroModelToJsonHelper.produceJson(mockResponse, wroManagerFactory);
    verify(mockResponse, times(1)).setContentType("application/json");
  }
}