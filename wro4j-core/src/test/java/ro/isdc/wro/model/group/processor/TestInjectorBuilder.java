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
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.AbstractDecorator;


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
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullWroManager() {
    new InjectorBuilder(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptWhenSettingNullWroManager() {
    new InjectorBuilder(null);
  }
  
  @Test
  public void shouldBuildInjectorWithValidWroManager() {
    final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
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
  public void shouldBuildValidInjectorWithBaseWromanagerFactory() {
    final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
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
    final ProcessorsFactory processorsFactory = Mockito.mock(ProcessorsFactory.class);
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    
    final BaseWroManagerFactory managerFactroy = new BaseWroManagerFactory();
    managerFactroy.setNamingStrategy(namingStrategy);
    managerFactroy.setProcessorsFactory(processorsFactory);
    managerFactroy.setUriLocatorFactory(uriLocatorFactory);
    
    final Injector injector = InjectorBuilder.create(managerFactroy).build();
    Assert.assertNotNull(injector);
    
    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertSame(namingStrategy, sample.namingStrategy);
    Assert.assertNotNull(sample.preProcessorExecutor);
    Assert.assertSame(processorsFactory, AbstractDecorator.getOriginalDecoratedObject(sample.processorsFactory));
    Assert.assertSame(uriLocatorFactory, AbstractDecorator.getOriginalDecoratedObject(sample.uriLocatorFactor));
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
    Assert.assertNotNull(sample.modelFactory);
    Assert.assertNotNull(sample.groupExtractor);
    Assert.assertNotNull(sample.cacheStrategy);
    Assert.assertNotNull(sample.hashBuilder);
  }
  
  @Test(expected = IOException.class)
  public void shouldInjectEachLocatorProvidedByLocatorFactory()
      throws Exception {
    final UriLocatorFactory uriLocatorFactory = new DefaultUriLocatorFactory();
    WroManagerFactory managerFactory = new BaseWroManagerFactory().setUriLocatorFactory(uriLocatorFactory);
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    
    final Sample sample = new Sample();
    injector.inject(sample);
    // this will throw NullPointerException if the uriLocator is not injected.
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
    @Inject
    CacheStrategy<?, ?> cacheStrategy;
    @Inject
    HashStrategy hashBuilder;
    @Inject
    ReadOnlyContext context;
  }
}
