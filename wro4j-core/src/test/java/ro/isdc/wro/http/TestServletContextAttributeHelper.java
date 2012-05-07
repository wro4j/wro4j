package ro.isdc.wro.http;

import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.ServletContextAttributeHelper.Attribute;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;

/**
 * @author Alex Objelean
 */
public class TestServletContextAttributeHelper {
  @Mock
  private ServletContext mockServletContext;
  private ServletContextAttributeHelper victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
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
    Attribute attr = Attribute.CONFIGURATION;
    WroConfiguration value = new WroConfiguration();
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
    victim.setAttribute(Attribute.WRO_MANAGER_FACTORY, "invalid type");
  }
  
  @Test
  public void shouldStoreAttributeOfValidType() {
    victim.setAttribute(Attribute.CONFIGURATION, new WroConfiguration());
  }
  

  @Test
  public void shouldStoreAttributeOfValidSubType() {
    victim.setAttribute(Attribute.WRO_MANAGER_FACTORY, new BaseWroManagerFactory());
  }
}
