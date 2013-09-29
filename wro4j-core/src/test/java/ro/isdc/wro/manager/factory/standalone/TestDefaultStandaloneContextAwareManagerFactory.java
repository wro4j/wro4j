package ro.isdc.wro.manager.factory.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestDefaultStandaloneContextAwareManagerFactory {
  private DefaultStandaloneContextAwareManagerFactory victim;

  @Before
  public void setUp() {
    victim = new DefaultStandaloneContextAwareManagerFactory();
    Context.set(Context.standaloneContext());
  }

  @Test(expected = NullPointerException.class)
  public void cannotInitializeWithNullContext() {
    victim.initialize(null);
  }

  @Test
  public void shouldNotChangeIgnoreMissingResourcesByDefault() {
    final StandaloneContext standaloneContext = new StandaloneContext();
    final boolean ignoreMissingResourcesBefore = Context.get().getConfig().isIgnoreMissingResources();
    victim.initialize(standaloneContext);
    final boolean ignoreMissingResourcesAfter = Context.get().getConfig().isIgnoreMissingResources();
    assertEquals(ignoreMissingResourcesBefore, ignoreMissingResourcesAfter);
    assertTrue(ignoreMissingResourcesAfter);
  }

  @Test
  public void shouldOverrideIgnoreMissingResourcesUsedInStandaloneContext() {
    final StandaloneContext standaloneContext = new StandaloneContext();
    standaloneContext.setIgnoreMissingResourcesAsString(Boolean.FALSE.toString());
    final boolean ignoreMissingResourcesBefore = Context.get().getConfig().isIgnoreMissingResources();
    victim.initialize(standaloneContext);
    final boolean ignoreMissingResourcesAfter = Context.get().getConfig().isIgnoreMissingResources();
    assertNotEquals(ignoreMissingResourcesBefore, ignoreMissingResourcesAfter);
    assertFalse(ignoreMissingResourcesAfter);
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
