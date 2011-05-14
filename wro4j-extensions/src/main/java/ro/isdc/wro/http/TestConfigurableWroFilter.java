/**
 * Copyright@2011
 */
package ro.isdc.wro.http;

import javax.servlet.FilterConfig;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * This test normally should live in wro4j-core module, but since we need spring dependency to test it, the test will
 * stay here (we have spring optional dependency in wro4j-extentions)
 *
 * @author Alex Objelean
 */
public class TestConfigurableWroFilter {
  @Mock
  private FilterConfig filterConfig;
  private ConfigurableWroFilter filter = null;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    final ApplicationContext ctx = new ClassPathXmlApplicationContext("configurableWroFilter-context.xml");
    filter = (ConfigurableWroFilter)ctx.getBean("filter");
  }

  @Test
  public void test() throws Exception {
    filter.init(filterConfig);
    final WroConfiguration config = filter.getWroConfiguration();
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(20, config.getModelUpdatePeriod());
    Assert.assertEquals(false, config.isGzipEnabled());
    Assert.assertEquals(true, config.isDebug());
    Assert.assertEquals(false, config.isIgnoreMissingResources());
    Assert.assertEquals(true, config.isDisableCache());
    Assert.assertEquals(false, config.isJmxEnabled());
  }
}
