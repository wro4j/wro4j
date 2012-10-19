/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.verify;
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
import ro.isdc.wro.cache.factory.CacheKeyFactory;
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
import ro.isdc.wro.util.WroUtil;


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
    assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    assertNotNull(sample.namingStrategy);
    assertNotNull(sample.preProcessorExecutor);
    assertNotNull(sample.processorsFactory);
    assertNotNull(sample.uriLocatorFactory);
    assertNotNull(sample.callbackRegistry);
    assertSame(injector, sample.injector);
    assertNotNull(sample.groupsProcessor);
  }

  @Test
  public void shouldBuildValidInjectorWithBaseWroManagerFactory() {
    final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
    assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    assertNotNull(sample.namingStrategy);
    assertNotNull(sample.preProcessorExecutor);
    assertNotNull(sample.processorsFactory);
    assertNotNull(sample.uriLocatorFactory);
    assertNotNull(sample.callbackRegistry);
    assertSame(injector, sample.injector);
    assertNotNull(sample.groupsProcessor);
    assertNotNull(sample.cacheKeyFactory);
  }

  @Test
  public void shouldBuildValidInjectorWithSomeFieldsSet() throws Exception {
    final NamingStrategy mockNamingStrategy = Mockito.mock(NamingStrategy.class);
    final ProcessorsFactory mockProcessorsFactory = Mockito.mock(ProcessorsFactory.class);
    final UriLocatorFactory mockLocatorFactory = Mockito.mock(UriLocatorFactory.class);

    final BaseWroManagerFactory managerFactroy = new BaseWroManagerFactory();
    managerFactroy.setNamingStrategy(mockNamingStrategy);
    managerFactroy.setProcessorsFactory(mockProcessorsFactory);
    managerFactroy.setUriLocatorFactory(mockLocatorFactory);

    final Injector injector = InjectorBuilder.create(managerFactroy).build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
//    assertTrue(Proxy.isProxyClass(sample.namingStrategy.getClass()));
    assertNotNull(sample.preProcessorExecutor);

    sample.namingStrategy.rename("", WroUtil.EMPTY_STREAM);
    verify(mockNamingStrategy).rename("", WroUtil.EMPTY_STREAM);

    sample.processorsFactory.getPostProcessors();
    verify(mockProcessorsFactory).getPostProcessors();

    sample.uriLocatorFactory.getInstance("");
    verify(mockLocatorFactory).getInstance("");

    assertNotNull(sample.callbackRegistry);
    assertSame(injector, sample.injector);
    assertNotNull(sample.groupsProcessor);
    assertNotNull(sample.modelFactory);
    assertNotNull(sample.groupExtractor);
    assertNotNull(sample.cacheStrategy);
    assertNotNull(sample.hashBuilder);
    assertNotNull(sample.readOnlyContext);
    assertNotNull(sample.cacheKeyFactory);
  }

  @Test(expected = IOException.class)
  public void shouldInjectEachLocatorProvidedByLocatorFactory()
      throws Exception {
    final UriLocatorFactory uriLocatorFactory = new DefaultUriLocatorFactory();
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setUriLocatorFactory(uriLocatorFactory);
    final Injector injector = InjectorBuilder.create(managerFactory).build();

    final Sample sample = new Sample();
    injector.inject(sample);
    // this will throw NullPointerException if the uriLocator is not injected.
    sample.uriLocatorFactory.locate("/path/to/servletContext/resource.js");
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private static class Sample {
    @Inject
    UriLocatorFactory uriLocatorFactory;
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
    ReadOnlyContext readOnlyContext;
    @Inject
    CacheKeyFactory cacheKeyFactory;
  }
}
