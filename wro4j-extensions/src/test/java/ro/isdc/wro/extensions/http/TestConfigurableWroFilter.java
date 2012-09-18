/**
 * Copyright@2011
 */
package ro.isdc.wro.extensions.http;

import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.http.ConfigurableWroFilter;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;

/**
 * This test normally should live in wro4j-core module, but since we need spring dependency to test it, the test will
 * stay here (we have spring optional dependency in wro4j-extentions)
 *
 * @author Alex Objelean
 */
public class TestConfigurableWroFilter {
  private static final Logger LOG = LoggerFactory.getLogger(TestConfigurableWroFilter.class);
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  private ConfigurableWroFilter filter = null;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    final ApplicationContext ctx = new ClassPathXmlApplicationContext("configurableWroFilter-context.xml");
    filter = (ConfigurableWroFilter)ctx.getBean("filter");
  }

  @Test
  public void shouldBeConfiguredBySpring() throws Exception {
    filter.init(mockFilterConfig);
    final WroConfiguration config = filter.getConfiguration();
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(20, config.getModelUpdatePeriod());
    Assert.assertEquals(false, config.isGzipEnabled());
    Assert.assertEquals(true, config.isDebug());
    Assert.assertEquals(false, config.isIgnoreMissingResources());
    Assert.assertEquals(true, config.isDisableCache());
    Assert.assertEquals(false, config.isJmxEnabled());
  }

  @Test
  public void shouldAcceptValidProcessorNameConfiguration()
      throws Exception {
    genericProcessorNameConfigurationTest(RhinoLessCssProcessor.ALIAS);
  }


  /**
   * To be reused by test from extensions module.
   */
  public void genericProcessorNameConfigurationTest(final String processorName)
      throws Exception {
    final ThreadLocal<Exception> processorsCreationException = new ThreadLocal<Exception>();
    try {
      final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
        @Override
        protected WroManagerFactory newWroManagerFactory() {
          final WroManagerFactory original = super.newWroManagerFactory();
          try {
            original.create().getProcessorsFactory().getPreProcessors();
          } catch (final Exception e) {
            LOG.debug("caught exception: ", e);
            processorsCreationException.set(e);
          }
          return original;
        }
      };
      final Properties properties = new Properties();
      properties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, processorName);
      filter.setProperties(properties);
      filter.init(mockFilterConfig);
    } catch (final Exception e) {
      LOG.error("exception occured", e);
      Assert.fail("Shouldn't fail with exception " + e.getMessage());
    }
    if (processorsCreationException.get() != null) {
      processorsCreationException.get();
    }
  }
}
