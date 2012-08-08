package ro.isdc.wro.manager.runnable;

import org.junit.Before;
import org.junit.Test;

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
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceWatcherRunnable {
  private ResourceWatcherRunnable victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    final WroModel model = new WroModel().addGroup(new Group("g1").addResource(Resource.create("/test.js")));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final WroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        WroTestUtils.createResourceMockingLocatorFactory());
    final Injector injector = InjectorBuilder.create(factory).build();
    victim = new ResourceWatcherRunnable(injector);
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
  public void test() {
    victim.run();
    victim.getCurrentHashes();
  }
}
