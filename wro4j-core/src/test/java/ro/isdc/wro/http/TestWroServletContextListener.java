package ro.isdc.wro.http;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test {@link WroServletContextListener} class.
 * @author Alex Objelean
 */
public class TestWroServletContextListener {
  @Mock
  private ServletContextEvent mockServletContextEvent;
  @Mock
  private ServletContext mockServletContext;
  private WroServletContextListener victim;
  private Map<String, Object> servletContextAttributes = new HashMap<String, Object>();
  @Before
  public void setUp() {
    initMocks(this);
    when(mockServletContextEvent.getServletContext()).thenReturn(mockServletContext);
    servletContextAttributes.clear();
    when(mockServletContext.getAttribute(Mockito.anyString())).then(new Answer<Object>() {
      public Object answer(final InvocationOnMock invocation)
          throws Throwable {
        return servletContextAttributes.get((String) invocation.getArguments()[0]);
      }
    });
    Mockito.doAnswer(new Answer<Object>() {
      public Object answer(final InvocationOnMock invocation)
          throws Throwable {
        final String key = (String) invocation.getArguments()[0];
        final Object value = invocation.getArguments()[1];
        return servletContextAttributes.put(key, value);
      }
    }).when(mockServletContext).setAttribute(Mockito.anyString(), Mockito.anyObject());
    victim = new WroServletContextListener();
  }

  @Test
  public void shouldCreateConfiguration() {
    victim.contextInitialized(mockServletContextEvent);
    assertNotNull(victim.getConfiguration());
  }
  
  @Test(expected = IllegalStateException.class)
  public void shouldFailWhenMultipleListenersWithSameNameDefined() {
    victim.contextInitialized(mockServletContextEvent);
    victim.contextInitialized(mockServletContextEvent);
  }
}
