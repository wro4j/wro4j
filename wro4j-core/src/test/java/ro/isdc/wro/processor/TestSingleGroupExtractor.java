/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.processor.impl.SingleGroupExtractor;
import ro.isdc.wro.resource.ResourceType;

/**
 * TestGroupsExtractor.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestSingleGroupExtractor {
  private SingleGroupExtractor groupsExtractor;

  @Before
  public void init() {
    groupsExtractor = new SingleGroupExtractor();
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotExtractResourceTypeUsingNullUri() {
    groupsExtractor.getResourceType(null);
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotExtractInvalidResourceType() {
    String uri = "/test.js";
    ResourceType type = groupsExtractor.getResourceType(uri);
    Assert.assertEquals(ResourceType.JS, type);

    uri = "/test.css";
    type = groupsExtractor.getResourceType(uri);
    Assert.assertEquals(ResourceType.CSS, type);

    uri = "/test.txt";
    groupsExtractor.getResourceType(uri);
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotExtractInvalidGroupName() {
    String uri = "/app/test.js";
    List<String> groupNames = groupsExtractor.getGroupNames(uri);
    Assert.assertEquals(1, groupNames.size());
    Assert.assertEquals("test", groupNames.get(0));

    uri = "/app/test.group.js";
    groupNames = groupsExtractor.getGroupNames(uri);
    Assert.assertEquals(1, groupNames.size());
    Assert.assertEquals("test.group", groupNames.get(0));

    uri = "/123/";
    groupsExtractor.getGroupNames(uri);
  }
}
