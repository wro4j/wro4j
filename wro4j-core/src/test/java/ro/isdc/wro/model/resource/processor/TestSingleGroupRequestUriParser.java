/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * TestGroupsExtractor.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestSingleGroupRequestUriParser {
  private DefaultGroupExtractor requestUriParser;

  @Before
  public void init() {
    requestUriParser = new DefaultGroupExtractor();
  }

  @Test(expected=NullPointerException.class)
  public void cannotExtractResourceTypeUsingNullUri() {
    requestUriParser.getResourceType(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotExtractGroupNamesUsingNullUri() {
    requestUriParser.getGroupName(null);
  }

  @Test
  public void testExtractInvalidResourceType() {
    String uri = "/test.js";
    ResourceType type = requestUriParser.getResourceType(mockRequestForUri(uri));
    Assert.assertEquals(ResourceType.JS, type);

    uri = "/test.css";
    type = requestUriParser.getResourceType(mockRequestForUri(uri));
    Assert.assertEquals(ResourceType.CSS, type);

    uri = "/test.txt";
    Assert.assertNull(requestUriParser.getResourceType(mockRequestForUri(uri)));
  }

  @Test
  public void testExtractNoGroupName() {
    String groupName = requestUriParser.getGroupName(mockRequestForUri("/app/test.js"));
    Assert.assertEquals("test", groupName);

    groupName = requestUriParser.getGroupName(mockRequestForUri("/app/test.group.js"));
    Assert.assertEquals("test.group", groupName);

    Assert.assertEquals(null, requestUriParser.getGroupName(mockRequestForUri("/123/")));
  }

  private HttpServletRequest mockRequestForUri(final String uri) {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn(uri);
    return request;
  }
}
