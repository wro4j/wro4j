package ro.isdc.wro.manager.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.factory.ConfigurableModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;


/**
 * @author Alex Objelean
 */
public class TestDefaultWroManagerFactory {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private ServletContext servletContext;
  private DefaultWroManagerFactory victim;

  @Before
  public void setUp()
      throws Exception {
    initMocks(this);
    victim = DefaultWroManagerFactory.create(new WroConfiguration());
    Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
    Mockito.when(servletContext.getResourceAsStream(Mockito.anyString())).then(createValidModelStreamAnswer());
    Context.set(Context.webContext(request, response, filterConfig));
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private Answer<InputStream> createValidModelStreamAnswer()
      throws Exception {
    return new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        return ClassLoader.getSystemResourceAsStream("wro.xml");
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullConfiguration() {
    final WroConfiguration config = null;
    DefaultWroManagerFactory.create(config);
  }

  @Test
  public void shouldCreateConfigurableManagerFactoryByDefault() {
    assertEquals(ConfigurableWroManagerFactory.class, victim.getFactory().getClass());
  }

  @Test
  public void shouldCreateOverridenManagerFactory() {
    victim = new DefaultWroManagerFactory(new Properties()) {
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
    victim = DefaultWroManagerFactory.create(config);
    assertEquals(NoProcessorsWroManagerFactory.class, victim.getFactory().getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotCreateInvalidConfiguredManagerFactory() {
    final WroConfiguration config = new WroConfiguration();
    config.setWroManagerClassName("invalid.class.name.ManagerFactory");
    victim = DefaultWroManagerFactory.create(config);
  }

  @Test
  public void shouldInvokeListenerMethods() {
    final WroManagerFactory mockManagerFactory = Mockito.mock(WroManagerFactory.class);
    victim = new DefaultWroManagerFactory(new Properties()) {
      @Override
      protected WroManagerFactory newManagerFactory() {
        return mockManagerFactory;
      }
    };
    victim.onCachePeriodChanged(0);
    verify(mockManagerFactory).onCachePeriodChanged(0);

    victim.onModelPeriodChanged(0);
    verify(mockManagerFactory).onModelPeriodChanged(0);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullProperty() {
    final Properties props = null;
    new DefaultWroManagerFactory(props);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidManagerClassConfiguredInProperties() {
    final Properties props = propsForWroManagerClassName("invalid");
    new DefaultWroManagerFactory(props);
  }

  @Test
  public void shouldLoadValidManagerClassConfiguredInProperties() {
    final Properties props = propsForWroManagerClassName(NoProcessorsWroManagerFactory.class.getName());
    final DefaultWroManagerFactory victim = new DefaultWroManagerFactory(props);
    assertEquals(NoProcessorsWroManagerFactory.class, victim.getFactory().getClass());
  }

  private Properties propsForWroManagerClassName(final String className) {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.managerFactoryClassName.name(), className);
    return props;
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

  /**
   * Exceptional flow for issue751.
   */
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenInvalidModelIsProvidedWhenUsingConfigurableWroManagerFactory() {
    useModelFactoryWithAlias("invalidModel");
  }

  /**
   * Happy flow for issue751.
   */
  @Test
  public void shouldUseValidModelIsProvidedWhenUsingConfigurableWroManagerFactory() {
    useModelFactoryWithAlias(XmlModelFactory.ALIAS);

  }

  private void useModelFactoryWithAlias(final String modelFactoryAlias) {
    final Properties properties = propsForWroManagerClassName(ConfigurableWroManagerFactory.class.getName());
    properties.setProperty(ConfigurableModelFactory.KEY, modelFactoryAlias);
    victim = new DefaultWroManagerFactory(properties);
    victim.create().getModelFactory().create();
  }
}
