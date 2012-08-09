package ro.isdc.wro.manager.runnable;

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

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcherRunnable {
  private static final String RESOURCE_URI = "/test.js";
  private static final String GROUP_NAME = "g1";
  private ResourceWatcherRunnable victim;
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
    victim = new ResourceWatcherRunnable(createInjector());
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
  public void cannotAcceptNullArgument() {
    new ResourceWatcherRunnable(null);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotCreateRunnableWhenRunningOutsideOfContext() {
    Context.unset();
    victim = new ResourceWatcherRunnable(WroTestUtils.createInjector());
  }
  
  @Test
  public void shouldPopulatePreviousHashesAfterFirstRun() {
    victim.run();
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
    victim = new ResourceWatcherRunnable(createInjector()) {
      @Override
      void onResourceChanged(final Group group, final Resource resource) {
        super.onResourceChanged(group, resource);
        Assert.assertEquals(GROUP_NAME, group.getName());
        Assert.assertEquals(RESOURCE_URI, resource.getUri());
        flag.set(Boolean.TRUE);
      }
    };
    victim.run();
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    
    Mockito.when(mockLocator.locate(Mockito.anyString())).thenReturn(
        new ByteArrayInputStream("different".getBytes()));

    victim.run();
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    assertTrue(flag.get());
  }

  @Test
  public void shouldAssumeResourceNotChangedWhenStreamIsUnavailable()
      throws Exception {
    victim = new ResourceWatcherRunnable(createInjector()) {
      @Override
      void onResourceChanged(final Group group, final Resource resource) {
        super.onResourceChanged(group, resource);
        Assert.fail("Should not detect the change");
      }
    };
    victim.run();
    assertEquals(1, victim.getPreviousHashes().keySet().size());
    
    Mockito.when(mockLocator.locate(Mockito.anyString())).thenThrow(new IOException("Resource is unavailable"));
    
    victim.run();
    assertEquals(1, victim.getPreviousHashes().keySet().size());
  }

}
