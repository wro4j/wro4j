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
public class TestLifecycleCallbackDecorator {
  private LifecycleCallbackDecorator decorator;


  @Test(expected=NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    decorator = new LifecycleCallbackDecorator(null);
  }

  @Test
  public void shouldCatchCallbacksExceptionsAndContinueExecution() {
    final LifecycleCallback callback = Mockito.spy(new LifecycleCallbackSupport());
    decorator = new LifecycleCallbackDecorator(callback);
    
    
    LifecycleCallbackRegistry registry = new LifecycleCallbackRegistry();
    registry.registerCallback(decorator);

    registry.onBeforeModelCreated();
    registry.onAfterModelCreated();
    registry.onBeforePreProcess();
    registry.onAfterPreProcess();
    registry.onBeforePostProcess();
    registry.onAfterPostProcess();
    registry.onBeforeProcess();
    registry.onAfterProcess();

    Mockito.verify(callback).onBeforeModelCreated();
    Mockito.verify(callback).onAfterModelCreated();
    Mockito.verify(callback).onBeforePreProcess();
    Mockito.verify(callback).onAfterPreProcess();
    Mockito.verify(callback).onBeforePostProcess();
    Mockito.verify(callback).onAfterPostProcess();
    Mockito.verify(callback).onBeforeProcess();
    Mockito.verify(callback).onAfterProcess();
  }
}
