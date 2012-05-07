package ro.isdc.wro.http;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
