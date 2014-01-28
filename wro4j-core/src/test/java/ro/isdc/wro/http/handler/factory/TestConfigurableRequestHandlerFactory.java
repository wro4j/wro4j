/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.http.handler.factory;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestConfigurableRequestHandlerFactory {
  private ConfigurableRequestHandlerFactory victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ConfigurableRequestHandlerFactory();
  }
  
  @Test
  public void shouldReturnEmptyListByDefault() {
    assertEquals(Collections.EMPTY_LIST, victim.getConfiguredStrategies());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testInvalidRequestHandlerSet() {
    final Properties props = new Properties();
    props.setProperty(ConfigurableRequestHandlerFactory.KEY, "invalid");
    victim.setProperties(props);
    victim.getConfiguredStrategies();
  }
}
