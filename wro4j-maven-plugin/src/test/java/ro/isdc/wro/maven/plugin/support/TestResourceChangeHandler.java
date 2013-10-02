package ro.isdc.wro.maven.plugin.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.Resource;


/**
 * @author Alex Objelean
 */
public class TestResourceChangeHandler {
  @Mock
  private BuildContextHolder buildContextHolder;
  @Mock
  private Log log;
  private WroManagerFactory managerFactory;
  private ResourceChangeHandler victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    managerFactory = new BaseWroManagerFactory();
    victim = new ResourceChangeHandler().setManagerFactory(managerFactory).setLog(log);
  }

  @Test(expected = NullPointerException.class)
  public void cannotUseUninitializedInstance() {
    victim = new ResourceChangeHandler();
    victim.isResourceChanged(Resource.create("1.js"));
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

  @After
  public void tearDown() {
    Context.unset();
  }
}
