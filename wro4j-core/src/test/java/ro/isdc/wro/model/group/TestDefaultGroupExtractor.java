/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Test class for {@link DefaultGroupExtractor}.
 *
 * @author Alex Objelean
 */
public class TestDefaultGroupExtractor {
  private GroupExtractor groupExtractor;
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
    // by default configuration is in debug mode
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(true);
    Context.set(Context.standaloneContext(), config);
    groupExtractor = new DefaultGroupExtractor();
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = NullPointerException.class)
  public void cannotExtractGroupNameWithNullUri() {
    groupExtractor.getGroupName(null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotExtractResourceTypeWithNullUri() {
    groupExtractor.getResourceType(null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotExtractMinimizedWithNullRequest() {
    groupExtractor.isMinimized(null);
  }

  @Test
  public void testValidCssUri() {
    final HttpServletRequest request = mockRequestForUri("group1.css");
    assertEquals("group1", groupExtractor.getGroupName(request));
    assertEquals(ResourceType.CSS, groupExtractor.getResourceType(request));
  }

  @Test
  public void testMinimizedWithoutParams() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    assertTrue(groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithTrueParam() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("true");
    assertTrue(groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithFalseParam() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("false");
    assertFalse(groupExtractor.isMinimized(request));
  }

  /**
   * Test that in DEPLOYMENT mode, the minimize flag cannot be false, no matter what parameter value is supplied.
   */
  @Test
  public void testMinimizedWithFalseParamInDEPLOYMENTMode() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(false);
    Context.get().setConfig(config);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("false");
    assertTrue(groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithInvalidParamValue() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("someInvalidBoolean");
    assertTrue(groupExtractor.isMinimized(request));
  }

  @Test
  public void testValidJsUri() {
    final HttpServletRequest request = mockRequestForUri("otherGroup.js");
    assertEquals("otherGroup", groupExtractor.getGroupName(request));
    assertEquals(ResourceType.JS, groupExtractor.getResourceType(request));
  }

  @Test
  public void shouldExtractGroupWhenUrlContainsJsessionID() {
    final HttpServletRequest request = mockRequestForUri("/contextPath/wro/my.css;jsessionid=blahblah");
    assertEquals("my", groupExtractor.getGroupName(request));
    assertEquals(ResourceType.CSS, groupExtractor.getResourceType(request));
  }

  @Test
  public void shouldExtractGroupWhenUrlContainsEncodedSession() {
    final HttpServletRequest request = mockRequestForUri("/contextPath/wro/all.js;jsessionID=A327EBE59831FF690C26B0B895EA877E");
    assertEquals("all", groupExtractor.getGroupName(request));
    assertEquals(ResourceType.JS, groupExtractor.getResourceType(request));
  }

  @Test
  public void shouldStripJsessionIDFromUrl() {
    final HttpServletRequest request = mockRequestForUri("https://www.servername.com:80/js/all.js;jsessionID=A327EBE59831FF690C26B0B895EA877EEFKDD&param.with.dot=value");
    assertEquals("all", groupExtractor.getGroupName(request));
    assertEquals(ResourceType.JS, groupExtractor.getResourceType(request));
  }

  @Test
  public void testWithInvalidUriType() {
    final HttpServletRequest request = mockRequestForUri("all.someInvalidType");
    assertEquals("all", groupExtractor.getGroupName(request));
    assertEquals(null, groupExtractor.getResourceType(request));
  }

  @Test
  public void testRequestDispatchedWithInclude() {
    final HttpServletRequest request = mockRequestForUri("all.someInvalidType");
    Mockito.when(request.getAttribute(DefaultGroupExtractor.ATTR_INCLUDE_PATH)).thenReturn("dispatched.type");
    assertEquals("dispatched", groupExtractor.getGroupName(request));
    assertEquals(null, groupExtractor.getResourceType(request));
  }

  private HttpServletRequest mockRequestForUri(final String uri) {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn(uri);
    return request;
  }
}
