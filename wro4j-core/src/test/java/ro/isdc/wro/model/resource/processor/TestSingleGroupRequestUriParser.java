/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

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

  @Test(expected=IllegalArgumentException.class)
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
    ResourceType type = requestUriParser.getResourceType(uri);
    Assert.assertEquals(ResourceType.JS, type);

    uri = "/test.css";
    type = requestUriParser.getResourceType(uri);
    Assert.assertEquals(ResourceType.CSS, type);

    uri = "/test.txt";
    Assert.assertNull(requestUriParser.getResourceType(uri));
  }

  @Test
  public void testExtractNoGroupName() {
    String uri = "/app/test.js";
    String groupName = requestUriParser.getGroupName(uri);
    Assert.assertEquals("test", groupName);

    uri = "/app/test.group.js";
    groupName = requestUriParser.getGroupName(uri);
    Assert.assertEquals("test.group", groupName);

    uri = "/123/";
    Assert.assertEquals(null, requestUriParser.getGroupName(uri));
  }
}
