package ro.isdc.wro.http.support;

import static org.junit.Assert.assertEquals;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.support.ServletContextAttributeHelper.Attribute;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.util.AbstractDecorator;

/**
 * @author Alex Objelean
 */
public class TestServletContextAttributeHelper {
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private FilterConfig mockFilterConfig;
  private ServletContextAttributeHelper victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    victim = new ServletContextAttributeHelper(mockServletContext, "value");
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullServletContextArgument() {
    victim = new ServletContextAttributeHelper(null, "value");
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullNameArgument() {
    victim = new ServletContextAttributeHelper(mockServletContext, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptEmptyNameArgument() {
    victim = new ServletContextAttributeHelper(mockServletContext, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptBlankNameArgument() {
    victim = new ServletContextAttributeHelper(mockServletContext, "   ");
  }

  @Test(expected = NullPointerException.class)
  public void cannotGetObjectForNullAttribute() {
    victim.getAttribute(null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetObjectForNullAttribute() {
    victim.setAttribute(null, null);
  }

  @Test
  public void shouldSetNullAttribute() {
    victim.setAttribute(Attribute.CONFIGURATION, null);
  }

  @Test
  public void shouldGetAttribute() {
    final Attribute attr = Attribute.CONFIGURATION;
    final WroConfiguration value = new WroConfiguration();
    Mockito.when(mockServletContext.getAttribute(victim.getAttributeName(attr))).thenReturn(value);
    Assert.assertSame(value, victim.getAttribute(attr));
  }

  @Test
  public void shouldClearAllAttributes() {
    victim.clear();
    //Mockito.verify(mockServletContext, Mockito.times(Attribute.values().length));
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotStoreAttributeOfInvalidType() {
    victim.setAttribute(Attribute.MANAGER_FACTORY, "invalid type");
  }

  @Test
  public void shouldStoreAttributeOfValidType() {
    victim.setAttribute(Attribute.CONFIGURATION, new WroConfiguration());
  }

  @Test
  public void shouldStoreAttributeOfValidSubType() {
    victim.setAttribute(Attribute.MANAGER_FACTORY, new BaseWroManagerFactory());
  }

  @Test(expected = NullPointerException.class)
  public void cannotUseNullFilterConfig() {
    ServletContextAttributeHelper.create(null);
  }

  @Test
  public void shouldCreateInstanceWhenValidFilterNameIsProvided() {
    final String filterName = "name";
    Mockito.when(mockFilterConfig.getInitParameter(ServletContextAttributeHelper.INIT_PARAM_NAME)).thenReturn(filterName);
    victim = ServletContextAttributeHelper.create(mockFilterConfig);
    Assert.assertEquals(filterName, victim.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenInitParamNameIsBlank() {
    Mockito.when(mockFilterConfig.getInitParameter(ServletContextAttributeHelper.INIT_PARAM_NAME)).thenReturn("  ");
    victim = ServletContextAttributeHelper.create(mockFilterConfig);
  }

  @Test
  public void shouldUseDefaultNameWhenInitParamNameIsNull() {
    Mockito.when(mockFilterConfig.getInitParameter(ServletContextAttributeHelper.INIT_PARAM_NAME)).thenReturn(null);
    victim = ServletContextAttributeHelper.create(mockFilterConfig);
    Assert.assertEquals(ServletContextAttributeHelper.DEFAULT_NAME, victim.getName());
  }


  @Test
  public void shouldLoadWroConfigurationFromServletContextAttribute() throws Exception {
    final WroFilter filter = new WroFilter();
    final WroConfiguration expectedConfig = new WroConfiguration();
    final ServletContextAttributeHelper helper = new ServletContextAttributeHelper(mockServletContext);
    Mockito.when(mockServletContext.getAttribute(helper.getAttributeName(Attribute.CONFIGURATION))).thenReturn(expectedConfig);
    filter.init(mockFilterConfig);
    Assert.assertSame(expectedConfig, filter.getConfiguration());
  }

  @Test
  public void shouldLoadWroManagerFactoryFromServletContextAttribute() throws Exception {
    final WroFilter filter = new WroFilter();
    final WroManagerFactory expectedManagerFactory = new BaseWroManagerFactory();
    final ServletContextAttributeHelper helper = new ServletContextAttributeHelper(mockServletContext);
    Mockito.when(mockServletContext.getAttribute(helper.getAttributeName(Attribute.MANAGER_FACTORY))).thenReturn(expectedManagerFactory);
    //reset it because it was initialized in test setup.
    filter.setWroManagerFactory(null);
    filter.init(mockFilterConfig);
    Assert.assertSame(expectedManagerFactory, AbstractDecorator.getOriginalDecoratedObject(filter.getWroManagerFactory()));
  }
}
