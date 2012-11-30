package ro.isdc.wro.model.resource.support.change;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcher {
  private static final String RESOURCE_URI = "/test.css";
  private static final String GROUP_NAME = "g1";
  private final CacheKey cacheEntry = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
  private ResourceWatcher victim;
  @Mock
  private UriLocator mockLocator;

  @Before
  public void setUp() {
    initMocks(this);
    Context.set(Context.standaloneContext());
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
    victim = new ResourceWatcher();
    createInjector().inject(victim);
  }

  public Injector createInjector() {
    final UriLocatorFactory locatorFactory = new AbstractUriLocatorFactory() {
      public UriLocator getInstance(final String uri) {
        return mockLocator;
      }
    };

    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_URI)));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final WroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    final Injector injector = InjectorBuilder.create(factory).build();
    return injector;
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckNullCacheEntry() {
    Context.unset();
    victim = new ResourceWatcher();
    victim.check(null);
  }

  @Test
  public void shouldNotDetectChangeAfterFirstRun() throws Exception {
    victim.check(cacheEntry);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));
  }

  @Test
  public void shouldDetectResourceChange() throws Exception {
    // flag used to assert that the expected code was invoked
    final AtomicBoolean flag = new AtomicBoolean(false);
    victim = new ResourceWatcher() {
      @Override
      void onGroupChanged(final CacheKey cacheEntry) {
        super.onGroupChanged(cacheEntry);
        Assert.assertEquals(GROUP_NAME, cacheEntry.getGroupName());
        flag.set(true);
      }
    };
    createInjector().inject(victim);
    victim.check(cacheEntry);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));

    Mockito.when(mockLocator.locate(Mockito.anyString())).thenReturn(
        new ByteArrayInputStream("different".getBytes()));

    victim.check(cacheEntry);
    assertTrue(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));
    assertTrue(flag.get());
  }

  @Test
  public void shouldAssumeResourceNotChangedWhenStreamIsUnavailable()
      throws Exception {
    victim = new ResourceWatcher() {
      @Override
      void onGroupChanged(final CacheKey cacheEntry) {
        super.onGroupChanged(cacheEntry);
        Assert.fail("Should not detect the change");
      }
    };
    createInjector().inject(victim);
    final ResourceChangeDetector mockChangeDetector = Mockito.spy(victim.getResourceChangeDetector());

    Mockito.when(mockLocator.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));

    victim.check(cacheEntry);
    verify(mockChangeDetector, never()).checkChangeForGroup(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void shouldDetectChangeOfImportedResource() throws Exception {
    final String importResourceUri = "imported.css";
    final AtomicBoolean groupChanged = new AtomicBoolean(false);
    final AtomicBoolean importResourceChanged = new AtomicBoolean(false);
    final CacheKey cacheEntry = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
    victim = new ResourceWatcher() {
      @Override
      void onResourceChanged(final Resource resource) {
        importResourceChanged.set(true);
      }
      @Override
      void onGroupChanged(final CacheKey key) {
        groupChanged.set(true);
      }
    };
    createInjector().inject(victim);
    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent("initial"));
    when(mockLocator.locate("/" + Mockito.eq(RESOURCE_URI))).thenAnswer(answerWithContent(String.format("@import url(%s)", importResourceUri)));

    victim.check(cacheEntry);

    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent("changed"));
    when(mockLocator.locate("/" + Mockito.eq(RESOURCE_URI))).thenAnswer(answerWithContent(String.format("@import url(%s)", importResourceUri)));

    victim.check(cacheEntry);
    assertTrue(groupChanged.get());
    assertTrue(importResourceChanged.get());
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
