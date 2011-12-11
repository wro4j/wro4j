/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import java.io.StringWriter;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroUtil;

/**
 * @author Alex Objelean
 */
public class TestLifecycleCallbackRegistry {
  private LifecycleCallbackRegistry registry;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    registry = new LifecycleCallbackRegistry();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected=NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    registry.registerCallback(null);
  }

  @Test
  public void shouldInvokeRegisteredCallbacks() {
    final LifecycleCallback callback = Mockito.mock(LifecycleCallback.class);
    registry.registerCallback(callback);

    registry.onBeforeModelCreated();
    Mockito.verify(callback).onBeforeModelCreated();

    registry.onAfterModelCreated();
    Mockito.verify(callback).onAfterModelCreated();

    registry.onBeforePreProcess();
    Mockito.verify(callback).onBeforePreProcess();

    registry.onAfterPreProcess();
    Mockito.verify(callback).onAfterPreProcess();

    registry.onBeforePostProcess();
    Mockito.verify(callback).onBeforePostProcess();

    registry.onAfterPostProcess();
    Mockito.verify(callback).onAfterPostProcess();
  }

  @Test
  public void shouldCatchCallbacksExceptionsAndContinueExecution() {
    final LifecycleCallback failingCallback = Mockito.mock(LifecycleCallback.class);
    final LifecycleCallback simpleCallback = Mockito.mock(LifecycleCallback.class);
    Mockito.doThrow(new IllegalStateException()).when(failingCallback).onBeforeModelCreated();

    registry.registerCallback(failingCallback);
    registry.registerCallback(simpleCallback);

    registry.onBeforeModelCreated();

    Mockito.verify(simpleCallback).onBeforeModelCreated();
  }

  /**
   * TODO: Simplify the test and move common usage to utility method.
   */
  @Test
  public void shouldInvokeCallbackWhenCallingProcess() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);;

    Mockito.when(response.getOutputStream()).thenReturn(
      new DelegatingServletOutputStream(new WriterOutputStream(new StringWriter())));
    Mockito.when(request.getRequestURI()).thenReturn("");
    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));


    final LifecycleCallback callback = Mockito.mock(LifecycleCallback.class);

    final String groupName = "group";

    final GroupExtractor groupExtractor = Mockito.mock(GroupExtractor.class);
    Mockito.when(groupExtractor.getGroupName(Mockito.any(HttpServletRequest.class))).thenReturn(groupName);
    Mockito.when(groupExtractor.getResourceType(Mockito.any(HttpServletRequest.class))).thenReturn(ResourceType.JS);

    final WroModelFactory modelFactory = WroUtil.factoryFor(new WroModel().addGroup(new Group(groupName)));

    final WroManager manager = new BaseWroManagerFactory().setGroupExtractor(groupExtractor).setModelFactory(
      modelFactory).create();
    manager.getCallbackRegistry().registerCallback(callback);
    manager.process();

    Mockito.verify(callback).onBeforeModelCreated();
    Mockito.verify(callback).onAfterModelCreated();
    Mockito.verifyNoMoreInteractions(callback);
  }
}
