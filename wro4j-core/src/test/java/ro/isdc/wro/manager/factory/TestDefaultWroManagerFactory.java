package ro.isdc.wro.manager.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * @author Alex Objelean
 */
public class TestDefaultWroManagerFactory {
  private DefaultWroManagerFactory victim;
  
  @Before
  public void setUp() {
    victim = new DefaultWroManagerFactory(new WroConfiguration());
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullConfiguration() {
    new DefaultWroManagerFactory(null);
  }
  
  @Test
  public void shouldCreateADefaultManagerFactory() {
    Assert.assertEquals(BaseWroManagerFactory.class, victim.getFactory().getClass());
  }
  
  @Test
  public void shouldCreateOverridenManagerFactory() {
    victim = new DefaultWroManagerFactory(new WroConfiguration()) {
      @Override
      protected WroManagerFactory newManagerFactory() {
        return new ConfigurableWroManagerFactory();
      }
    };
    Assert.assertEquals(ConfigurableWroManagerFactory.class, victim.getFactory().getClass());
  }
  

  @Test
  public void shouldCreateManagerFactory() {
    WroConfiguration config = new WroConfiguration();
    config.setWroManagerClassName(NoProcessorsWroManagerFactory.class.getName());
    victim = new DefaultWroManagerFactory(config);
    Assert.assertEquals(NoProcessorsWroManagerFactory.class, victim.getFactory().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotCreateInvalidConfiguredManagerFactory() {
    WroConfiguration config = new WroConfiguration();
    config.setWroManagerClassName("invalid.class.name.ManagerFactory");
    victim = new DefaultWroManagerFactory(config);
  }
}
