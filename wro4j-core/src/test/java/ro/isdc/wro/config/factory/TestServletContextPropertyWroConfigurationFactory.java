package ro.isdc.wro.config.factory;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.jmx.ConfigConstants;

/**
 * @author Alex Objelean
 */
public class TestServletContextPropertyWroConfigurationFactory {
  @Mock
  private ServletContext mockServletContext;
  private ServletContextPropertyWroConfigurationFactory victim;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream("".getBytes()));
    victim = new ServletContextPropertyWroConfigurationFactory(mockServletContext);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument() {
    new ServletContextPropertyWroConfigurationFactory(null);
  }

  @Test
  public void shouldBuildDefaultConfigurationWhenConfigStreamIsUnavailable() {
    Mockito.when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenThrow(new RuntimeException("BOOM!"));
    victim = new ServletContextPropertyWroConfigurationFactory(mockServletContext);
    Assert.assertNotNull(victim.create());
  }

  @Test
  public void shouldBuildDefaultConfigurationWhenConfigStreamIsNull() {
    Mockito.when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenReturn(null);
    victim = new ServletContextPropertyWroConfigurationFactory(mockServletContext);
    Assert.assertNotNull(victim.create());
  }

  @Test
  public void shouldCreateConfigFromValidLocation() {
    Assert.assertNotNull(victim.create());
  }


  @Test
  public void shouldCreateConfigOverridenPath() {
    victim = new ServletContextPropertyWroConfigurationFactory(mockServletContext) {
      @Override
      protected String getConfigPath() {
        return "/path/to/another/config.properties";
      }
    };
    Assert.assertNotNull(victim.create());
  }

  @Test
  public void shouldCreateConfigFromValidLocationAndOverrideAProperty() throws Exception {
    final long connectionTimeout = 10000;
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.connectionTimeout.name(), String.valueOf(connectionTimeout));
    final StringWriter propertiesWriter = new StringWriter();
    props.store(new WriterOutputStream(propertiesWriter), "");
    Mockito.when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenReturn(
        new ByteArrayInputStream(propertiesWriter.toString().getBytes()));
    Assert.assertEquals(connectionTimeout, victim.create().getConnectionTimeout());
  }
}
