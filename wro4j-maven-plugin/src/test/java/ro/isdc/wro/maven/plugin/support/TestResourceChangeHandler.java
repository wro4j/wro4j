package ro.isdc.wro.maven.plugin.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;


/**
 * @author Alex Objelean
 */
public class TestResourceChangeHandler {
  @Mock
  private BuildContextHolder buildContextHolder;
  @Mock
  private Log log;
  private ConfigurableWroManagerFactory managerFactory;
  private ResourceChangeHandler victim;

  @Before
  public void setUp() {
    initMocks(this);

    Context.set(Context.standaloneContext());

    managerFactory = new ConfigurableWroManagerFactory() {
      @Override
      protected Properties createProperties() {
        return new Properties();
      }
    };
    managerFactory.initialize(new StandaloneContext());

    victim = ResourceChangeHandler.create(managerFactory, log);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptInvalidManagerFactory() {
    victim = ResourceChangeHandler.create(null, log);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptInvalidLog() {
    victim = ResourceChangeHandler.create(managerFactory, null);
  }

  @Test
  public void shouldNotBeIncrementalBuildByDefault() {
    assertEquals(false, victim.isIncrementalBuild());
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckNullResourceForChange() {
    assertEquals(false, victim.isResourceChanged(null));
  }

  @Test
  public void shouldConsiderInvalidResourceAsUnchanged() {
    assertEquals(false, victim.isResourceChanged(Resource.create("/1.js")));
  }

  @Test
  public void shoulDestroyBuildContextHolder() {
    victim.setBuildContextHolder(buildContextHolder);
    victim.destroy();
    verify(buildContextHolder).destroy();
  }

  @Test
  public void shouldIdentifyChangeAfterDestroyOrForgetIsInvoked() {
    final String resourceUri = ClasspathUriLocator.createUri(getClass().getName().replace(".", "/") + ".class");
    final Resource resource = Resource.create(resourceUri, ResourceType.JS);
    assertEquals(true, victim.isResourceChanged(resource));
    victim.remember(resource);
    assertEquals(false, victim.isResourceChanged(resource));
    victim.forget(resource);
    assertEquals(true, victim.isResourceChanged(resource));
    victim.remember(resource);
    assertEquals(false, victim.isResourceChanged(resource));
    victim.destroy();
    assertEquals(true, victim.isResourceChanged(resource));
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
