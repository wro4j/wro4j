/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.ConfigurationContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test class for {@link DefaultGroupExtractor}.
 *
 * @author Alex Objelean
 */
public class TestDefaultGroupExtractor {
  private GroupExtractor groupExtractor;

  @Before
  public void setUp() {
    //by default configuration is in debug mode
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(true);
    ConfigurationContext.get().setConfig(config);
    groupExtractor = new DefaultGroupExtractor();
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotExtractGroupNameWithNullUri() {
    groupExtractor.getGroupName(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotExtractResourceTypeWithNullUri() {
    groupExtractor.getResourceType(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotExtractMinimizedWithNullRequest() {
    groupExtractor.isMinimized(null);
  }

  @Test
  public void testValidCssUri() {
    final String uri = "group1.css";
    Assert.assertEquals("group1", groupExtractor.getGroupName(uri));
    Assert.assertEquals(ResourceType.CSS, groupExtractor.getResourceType(uri));
  }

  @Test
  public void testMinimizedWithoutParams() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Assert.assertEquals(true, groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithTrueParam() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("true");
    Assert.assertEquals(true, groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithFalseParam() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("false");
    Assert.assertEquals(false, groupExtractor.isMinimized(request));
  }

  /**
   * Test that in DEPLOYMENT mode, the minimize flag cannot be false, no matter what parameter value is supplied.
   */
  @Test
  public void testMinimizedWithFalseParamInDEPLOYMENTMode() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(false);
    ConfigurationContext.get().setConfig(config);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("false");
    Assert.assertEquals(true, groupExtractor.isMinimized(request));
  }

  @Test
  public void testMinimizedWithInvalidParamValue() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(DefaultGroupExtractor.PARAM_MINIMIZE)).thenReturn("someInvalidBoolean");
    Assert.assertEquals(true, groupExtractor.isMinimized(request));
  }

  @Test
  public void testValidJsUri() {
    final String uri = "otherGroup.js";
    Assert.assertEquals("otherGroup", groupExtractor.getGroupName(uri));
    Assert.assertEquals(ResourceType.JS, groupExtractor.getResourceType(uri));
  }

  @Test
  public void testWithInvalidUriType() {
    final String uri = "all.someInvalidType";
    Assert.assertEquals("all", groupExtractor.getGroupName(uri));
    Assert.assertEquals(null, groupExtractor.getResourceType(uri));
  }

}
