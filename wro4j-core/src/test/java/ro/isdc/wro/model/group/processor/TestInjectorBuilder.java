/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static ro.isdc.wro.config.Context.set;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.ResourceBundleProcessor;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
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

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

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
    assertNotNull(sample.metaDataFactory);
    assertNotNull(sample.bundleProcessor);
    assertNotNull(sample.resourceWatcher);
    assertNotNull(sample.dispatcherLocator);
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
    assertNotNull(sample.metaDataFactory);
    assertNotNull(sample.cacheKeyFactory);
    assertNotNull(sample.bundleProcessor);
    assertNotNull(sample.dispatcherLocator);
  }

  @Test
  public void shouldBuildValidInjectorWithFewFieldsSet()
      throws Exception {
    final NamingStrategy mockNamingStrategy = mock(NamingStrategy.class);
    final ProcessorsFactory mockProcessorsFactory = mock(ProcessorsFactory.class);
    final UriLocatorFactory mockLocatorFactory = mock(UriLocatorFactory.class);
    final MetaDataFactory mockMetaDataFactory = Mockito.mock(MetaDataFactory.class);

    final BaseWroManagerFactory managerFactroy = new BaseWroManagerFactory();
    managerFactroy.setNamingStrategy(mockNamingStrategy);
    managerFactroy.setProcessorsFactory(mockProcessorsFactory);
    managerFactroy.setUriLocatorFactory(mockLocatorFactory);
    managerFactroy.setMetaDataFactory(mockMetaDataFactory);

    final Injector injector = InjectorBuilder.create(managerFactroy).build();
    assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    assertNotNull(sample.preProcessorExecutor);

    sample.namingStrategy.rename("", WroUtil.EMPTY_STREAM);
    verify(mockNamingStrategy).rename("", WroUtil.EMPTY_STREAM);

    sample.processorsFactory.getPostProcessors();
    verify(mockProcessorsFactory).getPostProcessors();

    sample.uriLocatorFactory.getInstance("");
    verify(mockLocatorFactory).getInstance("");

    sample.metaDataFactory.create();
    verify(mockMetaDataFactory).create();

    assertNotNull(sample.callbackRegistry);
    assertSame(injector, sample.injector);
    assertNotNull(sample.groupsProcessor);
    assertNotNull(sample.modelFactory);
    assertNotNull(sample.groupExtractor);
    assertNotNull(sample.cacheStrategy);
    assertNotNull(sample.hashBuilder);
    assertNotNull(sample.readOnlyContext);
    assertNotNull(sample.metaDataFactory);
    assertNotNull(sample.cacheKeyFactory);
    assertNotNull(sample.bundleProcessor);
    assertNotNull(sample.dispatcherLocator);
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

  @Test
  public void shouldOverrideTheDispatcherLocatorTimeoutWithConfiguredTimeout() {
    final Sample sample = new Sample();
    final DispatcherStreamLocator dispatcherLocator = new DispatcherStreamLocator();
    sample.dispatcherLocator = dispatcherLocator;

    assertEquals(WroConfiguration.DEFAULT_CONNECTION_TIMEOUT, sample.dispatcherLocator.getTimeout());

    final int timeout = 5000;
    Context.get().getConfig().setConnectionTimeout(timeout);
    final Injector injector = createDefaultInjector();
    injector.inject(sample);

    assertEquals(timeout, sample.dispatcherLocator.getTimeout());
  }

  private Injector createDefaultInjector() {
    return new InjectorBuilder(new BaseWroManagerFactory()).build();
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
    MetaDataFactory metaDataFactory;
    @Inject
    CacheKeyFactory cacheKeyFactory;
    @Inject
    ResourceBundleProcessor bundleProcessor;
    @Inject
    ResourceWatcher resourceWatcher;
    @Inject
    DispatcherStreamLocator dispatcherLocator;
  }
}
