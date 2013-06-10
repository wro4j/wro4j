package ro.isdc.wro.manager.factory;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.ConfigConstants;
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
    final WroConfiguration config = null;
    new DefaultWroManagerFactory(config);
  }

  @Test
  public void shouldCreateADefaultManagerFactory() {
    assertEquals(BaseWroManagerFactory.class, victim.getFactory().getClass());
  }

  @Test
  public void shouldCreateOverridenManagerFactory() {
    victim = new DefaultWroManagerFactory(new WroConfiguration()) {
      @Override
      protected WroManagerFactory newManagerFactory() {
        return new ConfigurableWroManagerFactory();
      }
    };
    assertEquals(ConfigurableWroManagerFactory.class, victim.getFactory().getClass());
  }


  @Test
  public void shouldCreateManagerFactory() {
    final WroConfiguration config = new WroConfiguration();
    config.setWroManagerClassName(NoProcessorsWroManagerFactory.class.getName());
    victim = new DefaultWroManagerFactory(config);
    assertEquals(NoProcessorsWroManagerFactory.class, victim.getFactory().getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotCreateInvalidConfiguredManagerFactory() {
    final WroConfiguration config = new WroConfiguration();
    config.setWroManagerClassName("invalid.class.name.ManagerFactory");
    victim = new DefaultWroManagerFactory(config);
  }

  @Test
  public void shouldInvokeListenerMethods() {
    final WroManagerFactory mockManagerFactory = Mockito.mock(WroManagerFactory.class);
    victim = new DefaultWroManagerFactory(new WroConfiguration()) {
      @Override
      protected WroManagerFactory newManagerFactory() {
        return mockManagerFactory;
      }
    };
    victim.onCachePeriodChanged(0);
    Mockito.verify(mockManagerFactory).onCachePeriodChanged(0);

    victim.onModelPeriodChanged(0);
    Mockito.verify(mockManagerFactory).onModelPeriodChanged(0);
  }


  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullProperty() {
    final Properties props = null;
    new DefaultWroManagerFactory(props);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidManagerClassConfiguredInProperties() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.managerFactoryClassName.name(), "invalid");
    new DefaultWroManagerFactory(props);
  }

  @Test
  public void shouldLoadValidManagerClassConfiguredInProperties() {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.managerFactoryClassName.name(), NoProcessorsWroManagerFactory.class.getName());
    final DefaultWroManagerFactory victim = new DefaultWroManagerFactory(props);
    assertEquals(NoProcessorsWroManagerFactory.class, victim.getFactory().getClass());
  }

  @Test
  public void shouldCreateOverridenManagerFactoryWhenManagerClassPropertyIsMissing() {
    victim = new DefaultWroManagerFactory(new Properties()) {
      @Override
      protected WroManagerFactory newManagerFactory() {
        return new ConfigurableWroManagerFactory();
      }
    };
    assertEquals(ConfigurableWroManagerFactory.class, victim.getFactory().getClass());
  }
}
