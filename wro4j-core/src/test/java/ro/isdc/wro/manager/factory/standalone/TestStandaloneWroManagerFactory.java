package ro.isdc.wro.manager.factory.standalone;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;

/**
 * @author Alex Objelean
 */
public class TestStandaloneWroManagerFactory {
  private StandaloneWroManagerFactory victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new StandaloneWroManagerFactory();
  }

  @Test
  public void shouldCreateValidWroManager() {
    assertNotNull(victim.create());
  }
}
