package ro.isdc.wro.model.resource.support.change;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestResourceChangeDetector {
  private static final String GROUP1_NAME = "g1";
  private static final String GROUP2_NAME = "g2";

  private ResourceChangeDetector victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new ResourceChangeDetector();
    final WroManagerFactory managerFactory = new BaseWroManagerFactory()
        .setUriLocatorFactory(new SimpleUriLocatorFactory().addLocator(WroTestUtils.createResourceMockingLocator()));
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckInvalidUriForChange()
      throws Exception {
    victim.checkChangeForGroup(null, GROUP1_NAME);
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckInvalidGroupNameForChange()
      throws Exception {
    victim.checkChangeForGroup("resource", null);
  }

  @Test
  public void shouldRequireChangeAtFirstCheck()
      throws Exception {
    assertTrue(victim.checkChangeForGroup("resource", GROUP1_NAME));
  }


  @Test
  public void shouldRequireChangeAfterReset()
      throws Exception {
    victim.checkChangeForGroup("resource", GROUP1_NAME);
    victim.reset();
    assertFalse(victim.checkChangeForGroup("resource", GROUP1_NAME));
    assertTrue(victim.checkChangeForGroup("resource", GROUP2_NAME));
  }
}
