/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;


/**
 * @author Alex Objelean
 * @created Created on Jul 25, 2010
 */
public class TestConfigurableWroFilter {
  private static final Logger LOG = LoggerFactory.getLogger(TestConfigurableWroFilter.class);
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterChain mockFilterChain;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
  }

  @Test
  public void testFilterWithPropertiesSet()
      throws Exception {
    final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        Assert.assertEquals(10, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    final Properties properties = new Properties();
    properties.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "10");
    filter.setProperties(properties);
    filter.init(Mockito.mock(FilterConfig.class));
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }

  @Test
  public void testFilterWithCacheUpdatePeriodSet()
      throws Exception {
    final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        Assert.assertEquals(20, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    filter.setCacheUpdatePeriod(20);
    filter.init(Mockito.mock(FilterConfig.class));
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidProcessorNameConfiguration()
      throws Exception {
    genericProcessorNameConfigurationTest("INVALID_PROCESSOR_NAME");
  }

  @Test
  public void shouldAcceptValidProcessorNameConfiguration()
      throws Exception {
    genericProcessorNameConfigurationTest(CssMinProcessor.ALIAS);
  }

  /**
   * To be reused by test from extensions module.
   */
  private static void genericProcessorNameConfigurationTest(final String processorName)
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
          return null;
        }
      };
      final Properties properties = new Properties();
      properties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, processorName);
      filter.setProperties(properties);
      filter.init(Mockito.mock(FilterConfig.class));
    } catch (final Exception e) {
      Assert.fail("Shouldn't fail with exception " + e.getMessage());
    }
    if (processorsCreationException.get() != null) {
      processorsCreationException.get();
      throw processorsCreationException.get();
    }
  }

  @After
  public void tearDown() {
    Context.unset();
  }

}
