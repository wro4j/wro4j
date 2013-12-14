/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager.Builder;
import ro.isdc.wro.manager.factory.DefaultWroManagerFactory;
import ro.isdc.wro.manager.factory.NoProcessorsWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactoryDecorator;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


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
  @Mock
  private ServletOutputStream mockServletOutputStream;
  private final ConfigurableWroFilter victim = new ConfigurableWroFilter();
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp()
      throws Exception {
    MockitoAnnotations.initMocks(this);
    when(mockRequest.getRequestURI()).thenReturn("/some.js");
    when(mockResponse.getOutputStream()).thenReturn(mockServletOutputStream);
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
  }
  
  @Test
  public void testFilterWithPropertiesSet()
      throws Exception {
    final ConfigurableWroFilter filter = new ConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertEquals(10, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    final Properties properties = new Properties();
    properties.setProperty(ConfigConstants.cacheUpdatePeriod.name(), "10");
    filter.setProperties(properties);
    filter.init(mockFilterConfig);
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }
  
  @Test
  public void testFilterWithCacheUpdatePeriodSet()
      throws Exception {
    final ConfigurableWroFilter filter = new SampleConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertEquals(20, Context.get().getConfig().getCacheUpdatePeriod());
      }
    };
    filter.setCacheUpdatePeriod(20);
    filter.init(mockFilterConfig);
    Context.unset();
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }
  
  @Test
  public void shouldUseDefaultEncodingWhenNoEncodingIsSet()
      throws Exception {
    final ConfigurableWroFilter filter = new SampleConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertEquals(WroConfiguration.DEFAULT_ENCODING, Context.get().getConfig().getEncoding());
      }
    };
    filter.setEncoding(null);
    filter.init(mockFilterConfig);
    Context.unset();
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }
  
  @Test
  public void shouldUseConfiguredEncodingWhenSet()
      throws Exception {
    final String encoding = "UTF-16";
    final ConfigurableWroFilter filter = new SampleConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertEquals(encoding, Context.get().getConfig().getEncoding());
      }
    };
    filter.setEncoding(encoding);
    filter.init(mockFilterConfig);
    Context.unset();
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }
  
  @Test
  public void shouldUseConfiguredMBeanNameWhenSet()
      throws Exception {
    final String mbeanName = "mbean";
    final ConfigurableWroFilter filter = new SampleConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertEquals(mbeanName, Context.get().getConfig().getMbeanName());
      }
    };
    filter.setMbeanName(mbeanName);
    filter.init(mockFilterConfig);
    Context.unset();
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);
  }
  
  @Test
  public void shouldUseNullMBeanWhenNotSet()
      throws Exception {
    final ConfigurableWroFilter filter = new SampleConfigurableWroFilter() {
      @Override
      protected void onRequestProcessed() {
        assertNull(Context.get().getConfig().getMbeanName());
      }
    };
    filter.setMbeanName(null);
    filter.init(mockFilterConfig);
    Context.unset();
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
    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
    genericProcessorNameConfigurationTest(CssMinProcessor.ALIAS);
  }
  
  /**
   * To be reused by test from extensions module.
   */
  private void genericProcessorNameConfigurationTest(final String processorName)
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
        
        @Override
        protected void onException(final Exception e, final HttpServletResponse response, final FilterChain chain) {
          throw WroRuntimeException.wrap(e);
        }
      };
      final Properties properties = new Properties();
      properties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, processorName);
      filter.setProperties(properties);
      filter.init(mockFilterConfig);
    } catch (final Exception e) {
      Assert.fail("Shouldn't fail with exception " + e.getMessage());
    }
    if (processorsCreationException.get() != null) {
      processorsCreationException.get();
      throw processorsCreationException.get();
    }
  }
  
  private static class SampleConfigurableWroFilter
      extends ConfigurableWroFilter {
    @Override
    protected WroManagerFactory newWroManagerFactory() {
      final WroManagerFactory factory = super.newWroManagerFactory();
      return new WroManagerFactoryDecorator(factory) {
        @Override
        protected void onBeforeBuild(final Builder builder) {
          final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(new Group("some")));
          builder.setModelFactory(modelFactory);
        }
      };
    }
    
    @Override
    protected void onException(final Exception e, final HttpServletResponse response, final FilterChain chain) {
      throw WroRuntimeException.wrap(e);
    }
  };
  
  @Test
  public void shouldUseCorrectWroManagerFactoryWhenPropertiesAreLoadedFromCustomLocation()
      throws Exception {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.managerFactoryClassName.name(), NoProcessorsWroManagerFactory.class.getName());
    victim.setProperties(props);
    victim.init(mockFilterConfig);
    final WroManagerFactory actual = ((DefaultWroManagerFactory) victim.getWroManagerFactory()).getFactory();
    assertEquals(NoProcessorsWroManagerFactory.class, actual.getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenAnInvalidWroManagerClassNameIsLoadedFromCustomLocation()
      throws Exception {
    final Properties props = new Properties();
    props.setProperty(ConfigConstants.managerFactoryClassName.name(), "invalid");
    victim.setProperties(props);
    victim.init(mockFilterConfig);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
}
