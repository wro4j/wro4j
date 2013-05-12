package ro.isdc.wro.model.resource.support.change;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
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
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcher {
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
  private final CacheKey cacheEntry = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
  private final CacheKey cacheEntry2 = new CacheKey(GROUP_2, ResourceType.JS, true);
  @Mock
  private ResourceLocator mockLocator;
  @Mock
  private ResourceLocatorFactory mockLocatorFactory;

  private ResourceWatcher victim;

  @Before
  public void setUp()
      throws Exception {
    initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new ResourceWatcher();
    when(mockLocatorFactory.getLocator(Mockito.anyString())).thenReturn(mockLocator);
    when(mockLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);
    when(mockLocator.getInputStream()).thenReturn(WroUtil.EMPTY_STREAM);

    createDefaultInjector().inject(victim);
  }

  public Injector createDefaultInjector()
      throws Exception {
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
    victim = new ResourceWatcher();
    victim.check(null);
  }

  @Test
  public void shouldNotDetectChangeAfterFirstRun() throws Exception {
    victim.check(cacheEntry);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));
  }

  @Test
  public void shouldDetectResourceChange()
      throws Exception {
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
    createDefaultInjector().inject(victim);
    victim.check(cacheEntry);
    assertFalse(victim.getResourceChangeDetector().checkChangeForGroup(RESOURCE_URI, GROUP_NAME));

    Mockito.when(mockLocatorFactory.locate(Mockito.anyString())).then(answerWithContent("different"));

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
    createDefaultInjector().inject(victim);
    final ResourceChangeDetector mockChangeDetector = Mockito.spy(victim.getResourceChangeDetector());

    Mockito.when(mockLocatorFactory.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));

    victim.check(cacheEntry);
    verify(mockChangeDetector, never()).checkChangeForGroup(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void shouldDetectChangeOfImportedResource()
      throws Exception {
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
    createDefaultInjector().inject(victim);
    when(mockLocator.getInputStream()).then(answerWithContent(String.format("@import url(%s)", importResourceUri)));

    final ResourceLocator mockImportedResourceLocator = mock(ResourceLocator.class);
    when(mockImportedResourceLocator.getInputStream()).then(answerWithContent("initial"));

    when(mockLocatorFactory.getLocator(Mockito.eq("/" + RESOURCE_URI))).thenReturn(mockLocator);
    when(mockLocatorFactory.getLocator(Mockito.eq("/" + importResourceUri))).thenReturn(mockImportedResourceLocator);

    victim.check(cacheEntry);

    when(mockImportedResourceLocator.getInputStream()).then(answerWithContent("changed"));

    victim.check(cacheEntry);
    assertTrue(groupChanged.get());
    assertTrue(importResourceChanged.get());
  }

  /**
   * Fix the issue described <a href="https://github.com/alexo/wro4j/issues/72">here</a>.
   */
  @Test
  public void shouldNotDetectErroneouslyChange() throws Exception {
    final AtomicBoolean groupChanged = new AtomicBoolean(false);
    final AtomicBoolean resourceChanged = new AtomicBoolean(false);
    victim = new ResourceWatcher() {
      @Override
      void onResourceChanged(final Resource resource) {
        resourceChanged.set(true);
      }
      @Override
      void onGroupChanged(final CacheKey key) {
        groupChanged.set(true);
      }
    };

    createDefaultInjector().inject(victim);
    //first check will always detect changes.
    victim.check(cacheEntry2);

    Mockito.when(mockLocatorFactory.locate(RESOURCE_FIRST)).then(answerWithContent("different"));

    victim.check(cacheEntry2);
    assertTrue(groupChanged.get());
    assertTrue(resourceChanged.get());

    groupChanged.set(false);
    resourceChanged.set(false);

    //next check should find no change
    victim.check(cacheEntry2);
    assertFalse(groupChanged.get());
    assertFalse(resourceChanged.get());
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
