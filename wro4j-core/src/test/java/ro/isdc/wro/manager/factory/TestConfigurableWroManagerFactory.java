/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ConfigurableCacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.factory.ConfigurableModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.ConfigurableLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.support.AbstractConfigurableMultipleStrategy;
import ro.isdc.wro.model.resource.support.hash.ConfigurableHashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.MD5HashStrategy;
import ro.isdc.wro.model.resource.support.naming.ConfigurableNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.FolderHashEncoderNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.TimestampNamingStrategy;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestConfigurableWroManagerFactory.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestConfigurableWroManagerFactory {
  private ConfigurableWroManagerFactory victim;
  @Mock
  private FilterConfig mockFilterConfig;
  private ConfigurableLocatorFactory uriLocatorFactory;
  @Mock
  private ServletContext mockServletContext;
  private ProcessorsFactory processorsFactory;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private Properties configProperties;

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
    // init context
    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    victim = new ConfigurableWroManagerFactory();
    configProperties = new Properties();
    victim.setConfigProperties(configProperties);
  }

  /**
   * Creates the manager and initialize processors with locators used for assetions.
   */
  private void createManager() {
    // create one instance for test
    final WroManager manager = victim.create();
    processorsFactory = manager.getProcessorsFactory();
    uriLocatorFactory = (ConfigurableLocatorFactory) AbstractDecorator.getOriginalDecoratedObject(manager.getUriLocatorFactory());
  }

  /**
   * When no uri locators are set, the default factory is used.
   */
  @Test
  public void shouldHaveNoLocatorsWhenNoUriLocatorsParamSet() {
    createManager();
    assertTrue(uriLocatorFactory.getConfiguredStrategies().isEmpty());
  }

  @Test
  public void shouldHaveNoLocatorsWhenNoLocatorsInitParamSet() {
    createManager();
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableLocatorFactory.PARAM_URI_LOCATORS)).thenReturn("");
    assertTrue(uriLocatorFactory.getConfiguredStrategies().isEmpty());
  }

  @Test
  public void shouldLoadUriLocatorsFromConfigurationFile() {
    configProperties.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, "servletContext");

    createManager();

    assertEquals(1, uriLocatorFactory.getConfiguredStrategies().size());
    assertSame(ServletContextUriLocator.class, uriLocatorFactory.getConfiguredStrategies().iterator().next().getClass());
  }

  @Test
  public void shouldLoadUriLocatorsFromFilterConfigRatherThanFromConfigProperties() {
    configProperties.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, "servletContext");
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableLocatorFactory.PARAM_URI_LOCATORS)).thenReturn(
        "classpath, servletContext");

    createManager();

    assertEquals(2, uriLocatorFactory.getConfiguredStrategies().size());
    final Iterator<UriLocator> locatorsIterator = uriLocatorFactory.getConfiguredStrategies().iterator();
    assertSame(ClasspathUriLocator.class, locatorsIterator.next().getClass());
    assertSame(ServletContextUriLocator.class, locatorsIterator.next().getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidUriLocatorsSet() {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableLocatorFactory.PARAM_URI_LOCATORS)).thenReturn(
        "INVALID1,INVALID2");

    createManager();

    uriLocatorFactory.getConfiguredStrategies();
  }

  @Test
  public void shouldHaveCorrectLocatorsSet() {
    configureValidUriLocators(mockFilterConfig);
    createManager();
    assertEquals(3, uriLocatorFactory.getConfiguredStrategies().size());
  }

  /**
   * @param filterConfig
   */
  private void configureValidUriLocators(final FilterConfig filterConfig) {
    Mockito.when(filterConfig.getInitParameter(ConfigurableLocatorFactory.PARAM_URI_LOCATORS)).thenReturn(
        ConfigurableLocatorFactory.createItemsAsString(ServletContextUriLocator.ALIAS, UrlUriLocator.ALIAS,
            ClasspathUriLocator.ALIAS));
  }

  @Test
  public void testProcessorsExecutionOrder() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        AbstractConfigurableMultipleStrategy.createItemsAsString(JSMinProcessor.ALIAS, CssImportPreProcessor.ALIAS,
            CssVariablesProcessor.ALIAS));
    final List<ResourcePreProcessor> list = (List<ResourcePreProcessor>) processorsFactory.getPreProcessors();
    assertEquals(JSMinProcessor.class, list.get(0).getClass());
    assertEquals(CssImportPreProcessor.class, list.get(1).getClass());
    assertEquals(CssVariablesProcessor.class, list.get(2).getClass());
  }

  @Test
  public void testWithEmptyPreProcessors() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn("");
    assertTrue(processorsFactory.getPreProcessors().isEmpty());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPreProcessorsSet() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    processorsFactory.getPreProcessors();
  }

  @Test
  public void testWhenValidPreProcessorsSet() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS)).thenReturn(
        "cssUrlRewriting");
    assertEquals(1, processorsFactory.getPreProcessors().size());
  }

  @Test
  public void testWithEmptyPostProcessors() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn("");
    assertTrue(processorsFactory.getPostProcessors().isEmpty());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidPostProcessorsSet() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
        "INVALID1,INVALID2");
    processorsFactory.getPostProcessors();
  }

  @Test
  public void testWhenValidPostProcessorsSet() {
    createManager();

    configureValidUriLocators(mockFilterConfig);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS)).thenReturn(
        "cssMinJawr, jsMin, cssVariables");
    assertEquals(3, processorsFactory.getPostProcessors().size());
  }

  @Test
  public void testConfigPropertiesWithValidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "cssMin");
    victim.setConfigProperties(configProperties);

    createManager();

    final Collection<ResourcePreProcessor> list = processorsFactory.getPreProcessors();
    assertEquals(1, list.size());
    assertEquals(CssMinProcessor.class, list.iterator().next().getClass());
  }

  @Test
  public void testConfigPropertiesWithValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin");
    victim.setConfigProperties(configProperties);

    createManager();

    assertEquals(1, processorsFactory.getPostProcessors().size());
    assertEquals(JSMinProcessor.class,
        ((ProcessorDecorator) processorsFactory.getPostProcessors().iterator().next()).getDecoratedObject().getClass());
  }

  @Test
  public void testConfigPropertiesWithMultipleValidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "jsMin, cssMin");
    victim.setConfigProperties(configProperties);

    createManager();

    assertEquals(2, processorsFactory.getPostProcessors().size());
    assertEquals(JSMinProcessor.class,
        ((ProcessorDecorator) processorsFactory.getPostProcessors().iterator().next()).getDecoratedObject().getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPreProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "INVALID");
    victim.setConfigProperties(configProperties);

    createManager();

    processorsFactory.getPreProcessors();
  }

  public void shouldUseExtensionAwareProcessorWhenProcessorNameContainsDotCharacter() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, "jsMin.js");
    victim.setConfigProperties(configProperties);
    assertEquals(1, processorsFactory.getPreProcessors().size());
    assertTrue(processorsFactory.getPreProcessors().iterator().next() instanceof ExtensionsAwareProcessorDecorator);
  }

  @Test(expected = WroRuntimeException.class)
  public void testConfigPropertiesWithInvalidPostProcessor() {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, "INVALID");
    victim.setConfigProperties(configProperties);

    createManager();

    processorsFactory.getPostProcessors();
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidNamingStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableNamingStrategy.KEY, "INVALID");
    victim.setConfigProperties(configProperties);
    victim.create().getNamingStrategy().rename("name", WroUtil.EMPTY_STREAM);
  }

  @Test
  public void shouldUseConfiguredNamingStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableNamingStrategy.KEY, TimestampNamingStrategy.ALIAS);
    victim.setConfigProperties(configProperties);
    final NamingStrategy actual = ((ConfigurableNamingStrategy) victim.create().getNamingStrategy()).getConfiguredStrategy();
    assertEquals(TimestampNamingStrategy.class, actual.getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidHashStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableHashStrategy.KEY, "INVALID");
    victim.setConfigProperties(configProperties);
    victim.create().getHashStrategy().getHash(WroUtil.EMPTY_STREAM);
  }

  @Test
  public void shouldUseConfiguredHashStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableHashStrategy.KEY, MD5HashStrategy.ALIAS);
    victim.setConfigProperties(configProperties);
    final HashStrategy actual = ((ConfigurableHashStrategy) victim.create().getHashStrategy()).getConfiguredStrategy();
    assertEquals(MD5HashStrategy.class, actual.getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidCacheStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableCacheStrategy.KEY, "INVALID");
    victim.setConfigProperties(configProperties);
    victim.create().getCacheStrategy().clear();
  }

  @Test
  public void shouldUseConfiguredCacheStrategy()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableCacheStrategy.KEY, MemoryCacheStrategy.ALIAS);
    victim.setConfigProperties(configProperties);

    final CacheStrategy<?, ?> actual = ((ConfigurableCacheStrategy) AbstractDecorator.getOriginalDecoratedObject(victim.create().getCacheStrategy())).getConfiguredStrategy();
    assertEquals(MemoryCacheStrategy.class, actual.getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidRequestHandler()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableCacheStrategy.KEY, "INVALID");
    victim.setConfigProperties(configProperties);
    victim.create().getCacheStrategy().clear();
  }

  @Test
  public void shouldUseConfiguredRequestHandler()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableCacheStrategy.KEY, MemoryCacheStrategy.ALIAS);
    victim.setConfigProperties(configProperties);
    final CacheStrategy<?, ?> actual = ((ConfigurableCacheStrategy) AbstractDecorator.getOriginalDecoratedObject(victim.create().getCacheStrategy())).getConfiguredStrategy();
    assertEquals(MemoryCacheStrategy.class, actual.getClass());
  }

  @Test
  public void shouldUseConfiguredModelFactory()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableModelFactory.KEY, XmlModelFactory.ALIAS);
    victim.setConfigProperties(configProperties);
    final WroModelFactory actual = ((ConfigurableModelFactory) AbstractDecorator.getOriginalDecoratedObject(victim.create().getModelFactory())).getConfiguredStrategy();
    assertEquals(XmlModelFactory.class, actual.getClass());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotUseInvalidConfiguredModelFactory()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableModelFactory.KEY, "invalid");
    victim.setConfigProperties(configProperties);
    ((ConfigurableModelFactory) AbstractDecorator.getOriginalDecoratedObject(victim.create().getModelFactory())).getConfiguredStrategy();
  }

  @Test
  public void shouldConsiderContributeMethodsWhenManagerFactoryIsExtended() {
    final String alias = "contributed";
    victim = new ConfigurableWroManagerFactory() {
      @Override
      protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {
        map.put(alias, new JSMinProcessor());
      }

      @Override
      protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {
        map.put(alias, new JSMinProcessor());
      }

      @Override
      protected void contributeLocators(final Map<String, UriLocator> map) {
        map.put(alias, new UrlUriLocator());
      }
    };
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, alias);
    configProperties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, alias);
    configProperties.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, alias);
    victim.setConfigProperties(configProperties);
    final WroManager manager = victim.create();

    assertFalse(manager.getProcessorsFactory().getPostProcessors().isEmpty());
    assertFalse(manager.getProcessorsFactory().getPreProcessors().isEmpty());
  }

  @Test
  public void shouldApplyNamingStrategyConcurrently()
      throws Exception {
    final Properties configProperties = new Properties();
    configProperties.setProperty(ConfigurableNamingStrategy.KEY, FolderHashEncoderNamingStrategy.ALIAS);
    victim.setConfigProperties(configProperties);
    WroTestUtils.runConcurrently(ContextPropagatingCallable.decorate(new Callable<Void>() {
      public Void call()
          throws Exception {
        victim.create().getNamingStrategy().rename("", new ByteArrayInputStream("".getBytes()));
        return null;
      }
    }));
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
