package ro.isdc.wro.model.resource.support.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import ro.isdc.wro.http.WroFilter;
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
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.change.ResourceWatcher.Callback;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(TestResourceWatcher.class);
  /**
   * The uri to the first resource in a group.
   */
  private static final String RESOURCE_JS_URI = "/path/1.js";
  private static final String RESOURCE_CSS_URI = "/test.css";
  private static final String GROUP_NAME = "g1";
  private static final String MIXED_GROUP_NAME = "mixedGroup";
  /**
   * Group containing two js resources.
   */
  private static final String GROUP_2 = "g2";
  private final CacheKey cacheKey = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
  private final CacheKey cacheKey2 = new CacheKey(GROUP_2, ResourceType.JS, true);
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private UriLocator mockLocator;
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
  public void setUp() {
    initMocks(this);
    Context.set(Context.webContext(request, response, filterConfig));
    // spy the interface instead of WroTestUtils.createResourceMockingLocator() because of mockito bug which was
    // reported on their mailing list.
    mockLocator = Mockito.spy(new UriLocator() {
      public InputStream locate(final String uri)
          throws IOException {
        return new ByteArrayInputStream(uri.getBytes());
      }

      public boolean accept(final String uri) {
        return true;
      }
    });
    // Add explicity the filter which makes the request allowed for async check
    when(request.getAttribute(Mockito.eq(WroFilter.ATTRIBUTE_PASSED_THROUGH_FILTER))).thenReturn(true);

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
    final UriLocatorFactory locatorFactory = new AbstractUriLocatorFactory() {
      public UriLocator getInstance(final String uri) {
        return mockLocator;
      }
    };

    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_CSS_URI)));
    model.addGroup(new Group(GROUP_2).addResource(Resource.create(RESOURCE_JS_URI)).addResource(
        Resource.create("/path/2.js")));
    model.addGroup(new Group(MIXED_GROUP_NAME).addResource(Resource.create(RESOURCE_CSS_URI)).addResource(
        Resource.create(RESOURCE_JS_URI)));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final WroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory).setCacheStrategy(cacheStrategy);
    return InjectorBuilder.create(factory).build();
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
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_CSS_URI, GROUP_NAME));
  }

  @Test
  public void shouldDetectResourceChange()
      throws Exception {
    // flag used to assert that the expected code was invoked
    createDefaultInjector().inject(victim);
    victim.check(cacheKey, resourceWatcherCallback);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_CSS_URI, GROUP_NAME));

    Mockito.when(mockLocator.locate(Mockito.anyString())).thenReturn(new ByteArrayInputStream("different".getBytes()));
    final ArgumentCaptor<CacheKey> argumentCaptor = ArgumentCaptor.forClass(CacheKey.class);

    victim.check(cacheKey);
    assertTrue(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_CSS_URI, GROUP_NAME));
    Mockito.verify(resourceWatcherCallback).onGroupChanged(argumentCaptor.capture());
    assertEquals(GROUP_NAME, argumentCaptor.getValue().getGroupName());
  }

  @Test
  public void shouldAssumeResourceNotChangedWhenStreamIsUnavailable()
      throws Exception {
    createDefaultInjector().inject(victim);
    final ResourceChangeDetector mockChangeDetector = Mockito.spy(victim.getResourceChangeDetector());

    Mockito.when(mockLocator.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));

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
    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent("initial"));
    when(mockLocator.locate("/" + Mockito.eq(RESOURCE_CSS_URI))).thenAnswer(
        answerWithContent(String.format("@import url(%s)", importResourceUri)));

    victim.check(cacheEntry, resourceWatcherCallback);

    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent("changed"));
    when(mockLocator.locate("/" + Mockito.eq(RESOURCE_CSS_URI))).thenAnswer(
        answerWithContent(String.format("@import url(%s)", importResourceUri)));

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
    victim.check(cacheKey2, resourceWatcherCallback);

    when(mockLocator.locate(RESOURCE_JS_URI)).thenAnswer(answerWithContent("changed"));

    victim.check(cacheKey2, resourceWatcherCallback);
    verify(resourceWatcherCallback, Mockito.atLeastOnce()).onGroupChanged(Mockito.any(CacheKey.class));
    verify(resourceWatcherCallback, Mockito.atLeastOnce()).onResourceChanged(Mockito.any(Resource.class));

    Mockito.reset(resourceWatcherCallback);

    // next check should find no change
    victim.check(cacheKey2, resourceWatcherCallback);
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
    final int timeout = 100;
    Context.get().getConfig().setConnectionTimeout(timeout);
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

    victim.tryAsyncCheck(cacheKey);
    WroTestUtils.waitUntil(new Function<Void, Boolean>() {
      public Boolean apply(final Void input)
          throws Exception {
        return asyncInvoker.get() != null;
      }
    }, timeout * 3);
    assertNotNull(asyncInvoker.get());
    assertNotNull(exceptionHolder.get());
    // We expect a request to fail, since a request a localhost using some port from where we expect to get no response.
    LOG.debug("Exception: {}", exceptionHolder.get().getClass());
    assertTrue(exceptionHolder.get() instanceof IOException);
  }

  @Test
  public void shouldNotCheckAtAllWhenAsyncIsConfiguredButNotAllowed() {
    Context.get().getConfig().setResourceWatcherAsync(true);
    when(request.getAttribute(Mockito.eq(WroFilter.ATTRIBUTE_PASSED_THROUGH_FILTER))).thenReturn(null);
    final ResourceWatcher victimSpy = Mockito.spy(victim);
    victimSpy.tryAsyncCheck(cacheKey);
    verify(victimSpy, Mockito.never()).check(Mockito.eq(cacheKey));
  }

  @Test
  public void shouldRemoveKeyFromCacheStrategyWhenChangeDetected() {
    victim.check(cacheKey);
    final CacheValue cacheValue = null;
    verify(cacheStrategy).put(Mockito.eq(cacheKey), Mockito.eq(cacheValue));
  }

  private Answer<InputStream> answerWithContent(final String content) {
    return answerWithContent(content, 0);
  }

  private Answer<InputStream> answerWithContent(final String content, final long delay) {
    return new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        if (delay > 0) {
          Thread.sleep(delay);
        }
        return new ByteArrayInputStream(content.getBytes());
      }
    };
  }

  @Test
  public void shouldCheckForResourceChangeAsynchronously()
      throws Exception {
    Context.get().getConfig().setResourceWatcherAsync(true);
    final CacheKey cacheKey1 = new CacheKey(MIXED_GROUP_NAME, ResourceType.CSS, true);
    final CacheKey cacheKey2 = new CacheKey(MIXED_GROUP_NAME, ResourceType.JS, true);
    // First check is required to ensure that the subsequent changes do not detect any change
    victim.check(cacheKey1);
    victim.check(cacheKey2);

    when(mockLocator.locate(RESOURCE_JS_URI)).thenAnswer(answerWithContent("changed"));
    victim.check(cacheKey2, resourceWatcherCallback);
    verify(resourceWatcherCallback).onGroupChanged(Mockito.any(CacheKey.class));
    verify(resourceWatcherCallback).onResourceChanged(Mockito.any(Resource.class));
  }

  private ContextPropagatingCallable<Void> createCheckingCallable(final CacheKey cacheKey, final Callback callback) {
    return new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        victim.check(cacheKey, callback);
        return null;
      }
    });
  }
}
