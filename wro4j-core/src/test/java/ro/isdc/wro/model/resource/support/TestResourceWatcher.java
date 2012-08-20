package ro.isdc.wro.model.resource.support;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import ro.isdc.wro.cache.CacheEntry;
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


/**
 * @author Alex Objelean
 */
public class TestResourceWatcher {
  private static final String RESOURCE_URI = "/test.js";
  private static final String GROUP_NAME = "g1";
  private final CacheEntry cacheEntry = new CacheEntry(GROUP_NAME, ResourceType.JS, true);
  private ResourceWatcher victim;
  @Mock
  private ResourceLocatorFactory mockLocator;
  
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
    final ResourceLocatorFactory locatorFactory = new AbstractUriLocatorFactory() {
      public ResourceLocator getInstance(final String uri) {
        return mockLocator;
      }
    };
    
    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_URI)));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final WroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setLocatorFactory(
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
  public void shouldPopulatePreviousHashesAfterFirstRun() {
    victim.check(cacheEntry);
    assertTrue(victim.getCurrentHashes().keySet().isEmpty());
    assertEquals(1, victim.getPreviousHashes().keySet().size());
  }
  
  @Test
  public void shouldDetectResourceChange() throws Exception {
    // flag used to assert that the expected code was invoked
    final ThreadLocal<Boolean> flag = new ThreadLocal<Boolean>() {
      @Override
      protected Boolean initialValue() {
        return Boolean.FALSE;
      }
    };
    victim = new ResourceWatcher() {
      @Override
      void onGroupChanged(final CacheEntry cacheEntry) {
        super.onGroupChanged(cacheEntry);
        Assert.assertEquals(GROUP_NAME, cacheEntry.getGroupName());
        flag.set(Boolean.TRUE);
      }
    };
    createInjector().inject(victim);
    victim.check(cacheEntry);
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    
    Mockito.when(mockLocator.locate(Mockito.anyString())).thenReturn(
        new ByteArrayInputStream("different".getBytes()));

    victim.check(cacheEntry);
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    assertTrue(flag.get());
  }

  @Test
  public void shouldAssumeResourceNotChangedWhenStreamIsUnavailable()
      throws Exception {
    victim = new ResourceWatcher() {
      @Override
      void onGroupChanged(final CacheEntry cacheEntry) {
        super.onGroupChanged(cacheEntry);
        Assert.fail("Should not detect the change");
      }
    };
    createInjector().inject(victim);
    victim.check(cacheEntry);
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    
    Mockito.when(mockLocator.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));
    
    victim.check(cacheEntry);
    assertEquals(1, victim.getPreviousHashes().keySet().size());
  }
}
