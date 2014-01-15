package ro.isdc.wro.model.resource.support.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.callback.LifecycleCallbackSupport;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher.Callback;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(TestResourceWatcher.class);
  /**
   * The uri to the first resource in a group.
   */
  private static final String RESOURCE_FIRST = "/path/1.js";
  private static final String RESOURCE_URI = "/test.css";
  private static final String GROUP_NAME = "g1";
  /**
   * Group containing two js resources.
   */
  private static final String GROUP_2 = "g2";
  private final CacheKey cacheKey = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
  private final CacheKey cacheEntry2 = new CacheKey(GROUP_2, ResourceType.JS, true);
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private ResourceLocator mockLocator;
  @Mock
  private ResourceLocatorFactory mockLocatorFactory;
  @Mock
  private Callback resourceWatcherCallback;
  @Mock
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;

  private ResourceWatcher victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp()
      throws Exception {
    initMocks(this);
    Context.set(Context.webContext(request, response, filterConfig));
    victim = new ResourceWatcher();
    when(mockLocatorFactory.getLocator(Mockito.anyString())).thenReturn(mockLocator);
    when(mockLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);

    victim = new ResourceWatcher();
    createDefaultInjector().inject(victim);
  }

  @After
  public void tearDown()
      throws Exception {
    victim.destroy();
    Context.unset();
  }

  public Injector createDefaultInjector() {
    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_URI)));
    model.addGroup(new Group(GROUP_2).addResource(Resource.create(RESOURCE_FIRST)).addResource(
        Resource.create("/path/2.js")));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final WroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setLocatorFactory(
        mockLocatorFactory);
    final Injector injector = InjectorBuilder.create(factory).build();
    return injector;
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckNullCacheEntry() {
    Context.unset();
    victim.check(null);
  }

  @Test
  public void shouldNotDetectChangeAfterFirstRun()
      throws Exception {
    victim.check(cacheKey);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));
  }

  @Test
  public void shouldDetectResourceChange()
      throws Exception {
    // flag used to assert that the expected code was invoked
    createDefaultInjector().inject(victim);
    victim.check(cacheKey, resourceWatcherCallback);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));

    Mockito.when(mockLocatorFactory.locate(Mockito.anyString())).then(answerWithContent("different"));
    final ArgumentCaptor<CacheKey> argumentCaptor = ArgumentCaptor.forClass(CacheKey.class);

    victim.check(cacheKey);
    assertTrue(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));
    Mockito.verify(resourceWatcherCallback).onGroupChanged(argumentCaptor.capture());
    assertEquals(GROUP_NAME, argumentCaptor.getValue().getGroupName());
  }

  @Test
  public void shouldAssumeResourceNotChangedWhenStreamIsUnavailable()
      throws Exception {
    createDefaultInjector().inject(victim);
    final ResourceChangeDetector mockChangeDetector = Mockito.spy(victim.getResourceChangeDetector());

    Mockito.when(mockLocatorFactory.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));

    victim.check(cacheKey, resourceWatcherCallback);
    verify(resourceWatcherCallback, never()).onGroupChanged(Mockito.any(CacheKey.class));
    verify(mockChangeDetector, never()).checkChangeForGroup(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void shouldDetectChangeOfImportedResource()
      throws Exception {
    final String importResourceUri = "imported.css";
    final CacheKey cacheEntry = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
    victim = new ResourceWatcher();
    createDefaultInjector().inject(victim);
    when(mockLocator.getInputStream()).then(answerWithContent(String.format("@import url(%s)", importResourceUri)));

    final ResourceLocator mockImportedResourceLocator = mock(ResourceLocator.class);
    when(mockImportedResourceLocator.getInputStream()).then(answerWithContent("initial"));

    when(mockLocatorFactory.getLocator(Mockito.eq("/" + RESOURCE_URI))).thenReturn(mockLocator);
    when(mockLocatorFactory.getLocator(Mockito.eq("/" + importResourceUri))).thenReturn(mockImportedResourceLocator);

    victim.check(cacheEntry);

    when(mockImportedResourceLocator.getInputStream()).then(answerWithContent("changed"));

    victim.check(cacheEntry);

    verify(resourceWatcherCallback).onGroupChanged(Mockito.any(CacheKey.class));
    verify(resourceWatcherCallback).onResourceChanged(Mockito.any(Resource.class));
  }

  /**
   * Fix the issue described <a href="https://github.com/alexo/wro4j/issues/72">here</a>.
   */
  @Test
  public void shouldNotDetectErroneouslyChange()
      throws Exception {
    createDefaultInjector().inject(victim);

    // first check will always detect changes.
    victim.check(cacheEntry2, resourceWatcherCallback);

    Mockito.when(mockLocatorFactory.locate(RESOURCE_FIRST)).then(answerWithContent("changed"));

    victim.check(cacheEntry2, resourceWatcherCallback);
    verify(resourceWatcherCallback, Mockito.atLeastOnce()).onGroupChanged(Mockito.any(CacheKey.class));
    verify(resourceWatcherCallback, Mockito.atLeastOnce()).onResourceChanged(Mockito.any(Resource.class));

    Mockito.reset(resourceWatcherCallback);

    // next check should find no change
    victim.check(cacheEntry2, resourceWatcherCallback);
    verify(resourceWatcherCallback, Mockito.never()).onGroupChanged(Mockito.any(CacheKey.class));
    verify(resourceWatcherCallback, Mockito.never()).onResourceChanged(Mockito.any(Resource.class));
  }

  private static class CallbackRegistryHolder {
    @Inject
    private LifecycleCallbackRegistry registry;
  }

  @Test
  public void shouldInvokeCallbackWhenChangeIsDetected()
      throws Exception {
    final CallbackRegistryHolder callbackRegistryHolder = new CallbackRegistryHolder();
    final AtomicBoolean flag = new AtomicBoolean();
    final Injector injector = createDefaultInjector();
    injector.inject(victim);
    injector.inject(callbackRegistryHolder);
    callbackRegistryHolder.registry.registerCallback(new ObjectFactory<LifecycleCallback>() {
      public LifecycleCallback create() {
        return new LifecycleCallbackSupport() {
          @Override
          public void onResourceChanged(final Resource resource) {
            flag.set(true);
          }
        };
      }
    });
    victim.check(cacheKey);
    assertTrue(flag.get());
  }

  @Test
  public void shouldCheckForChangeAsynchronously()
      throws Exception {
    final String invalidUrl = "http://localhost:1/";
    when(request.getRequestURL()).thenReturn(new StringBuffer(invalidUrl));
    when(request.getServletPath()).thenReturn("");
    final AtomicReference<Callable<Void>> asyncInvoker = new AtomicReference<Callable<Void>>();
    final AtomicReference<Exception> exceptionHolder = new AtomicReference<Exception>();
    victim = new ResourceWatcher() {
      @Override
      void submit(final Callable<Void> callable) {
        try {
          final Callable<Void> decorated = new ContextPropagatingCallable<Void>(callable) {
            @Override
            public Void call()
                throws Exception {
              try {
                callable.call();
                return null;
              } catch (final Exception e) {
                exceptionHolder.set(e);
                throw e;
              } finally {
                asyncInvoker.set(callable);
              }
            }
          };
          super.submit(decorated);
        } finally {
        }
      }
    };
    createDefaultInjector().inject(victim);

    Context.get().getConfig().setResourceWatcherAsync(true);

    victim.checkAsync(cacheKey);
    WroTestUtils.waitUntil(new Function<Void, Boolean>() {
      public Boolean apply(final Void input)
          throws Exception {
        return asyncInvoker.get() != null;
      }
    }, 2000);
    assertNotNull(asyncInvoker.get());
    assertNotNull(exceptionHolder.get());
    //We expect a request to fail, since a request a localhost using some port from where we expect to get no response.
    assertEquals(ConnectException.class, exceptionHolder.get().getClass());
  }

  @Test
  public void shouldRemoveKeyFromCacheStrategyWhenChangeDetected() {
    victim.check(cacheKey);
    final CacheValue cacheValue = null;
    verify(cacheStrategy).put(Mockito.eq(cacheKey), Mockito.eq(cacheValue));
  }

  private Answer<InputStream> answerWithContent(final String content) {
    return new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        return new ByteArrayInputStream(content.getBytes());
      }
    };
  }
}
