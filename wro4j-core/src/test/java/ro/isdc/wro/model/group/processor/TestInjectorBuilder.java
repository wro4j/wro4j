/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static ro.isdc.wro.config.Context.set;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactoryDecorator;
import ro.isdc.wro.model.resource.util.NamingStrategy;

/**
 * @author Alex Objelean
 * @created 8 Jan 2012
 */
public class TestInjectorBuilder {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  
  
  @Before
  public void setUp() {
    initMocks(this);
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    when(mockRequest.getServletPath()).thenReturn("");
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenReturn(null);
    set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
  }

  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullWroManager() {
    new InjectorBuilder(null);
  }

  @Test(expected=NullPointerException.class)
  public void cannotAcceptWhenSettingNullWroManager() {
    new InjectorBuilder().setWroManager(null);
  }

  @Test
  public void shouldBuildInjectorWithValidWroManager() {
    final WroManager manager = new BaseWroManagerFactory().create();
    final Injector injector = new InjectorBuilder(manager).build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertNotNull(sample.namingStrategy);
    Assert.assertNotNull(sample.preProcessorExecutor);
    Assert.assertNotNull(sample.processorsFactory);
    Assert.assertNotNull(sample.uriLocatorFactor);
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
  }

  @Test
  public void shouldBuildValidInjectorWithDefaultConstructor() {
    final Injector injector = new InjectorBuilder().build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertNotNull(sample.namingStrategy);
    Assert.assertNotNull(sample.preProcessorExecutor);
    Assert.assertNotNull(sample.processorsFactory);
    Assert.assertNotNull(sample.uriLocatorFactor);
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
  }

  @Test
  public void shouldBuildValidInjectorWithSomeFieldsSet() {
    final NamingStrategy namingStrategy = Mockito.mock(NamingStrategy.class);
    final PreProcessorExecutor preProcessorExecutor = Mockito.mock(PreProcessorExecutor.class);
    final ProcessorsFactory processorsFactory = Mockito.mock(ProcessorsFactory.class);
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    
    final WroManager manager = new BaseWroManagerFactory().create();
    
    final Injector injector = new InjectorBuilder(manager).setNamingStrategy(namingStrategy).setPreProcessorExecutor(
        preProcessorExecutor).setProcessorsFactory(processorsFactory).setUriLocatorFactory(uriLocatorFactory).build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertSame(namingStrategy, sample.namingStrategy);
    Assert.assertSame(preProcessorExecutor, sample.preProcessorExecutor);
    Assert.assertSame(processorsFactory, ((ProcessorsFactoryDecorator) sample.processorsFactory).getDecoratedObject());
    Assert.assertSame(uriLocatorFactory, ((UriLocatorFactoryDecorator) sample.uriLocatorFactor).getDecoratedObject());
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
    Assert.assertNotNull(sample.modelFactory);
    Assert.assertNotNull(sample.groupExtractor);
  }
  
  @Test(expected = IOException.class)
  public void shouldInjectEachLocatorProvidedByLocatorFactory() throws Exception {
    final UriLocatorFactory uriLocatorFactory = new DefaultUriLocatorFactory();
    final Injector injector = new InjectorBuilder().setUriLocatorFactory(uriLocatorFactory).build();
    
    final Sample sample = new Sample();
    injector.inject(sample);
    //this will throw NullPointerException if the uriLocator is not injected.
    sample.uriLocatorFactor.locate("/path/to/servletContext/resource.js");
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private static class Sample {
    @Inject
    UriLocatorFactory uriLocatorFactor;
    @Inject
    ProcessorsFactory processorsFactory;
    @Inject
    NamingStrategy namingStrategy;
    @Inject
    PreProcessorExecutor preProcessorExecutor;
    @Inject
    LifecycleCallbackRegistry callbackRegistry;
    @Inject
    Injector injector;
    @Inject
    GroupsProcessor groupsProcessor;
    @Inject
    WroModelFactory modelFactory;
    @Inject
    GroupExtractor groupExtractor;
  }
}
