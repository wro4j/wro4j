/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.processor.impl.UriProcessorImpl;
import ro.isdc.wro.resource.ResourceType;

/**
 * TestProcessor.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class TestUriProcessor {
  private UriProcessor uriProcessor;

  @Before
  public void init() {
    uriProcessor = new UriProcessorImpl();
  }

  @Test
  public void processResourceType() {
    String uri = "/test.js";
    ResourceType type = uriProcessor.getResourceType(uri);
    Assert.assertEquals(ResourceType.JS, type);

    uri = "/test.css";
    type = uriProcessor.getResourceType(uri);
    Assert.assertEquals(ResourceType.CSS, type);

    uri = "/test.txt";
    try {
      type = uriProcessor.getResourceType(uri);
      Assert.fail("Should have fail");
    } catch (final Exception e) {}
  }

  @Test
  public void processGroupNames() {
    String uri = "/app/test.js";
    List<String> groupNames = uriProcessor.getGroupNames(uri);
    Assert.assertEquals(1, groupNames.size());
    Assert.assertEquals("test", groupNames.get(0));

    uri = "/app/test.group.js";
    groupNames = uriProcessor.getGroupNames(uri);
    Assert.assertEquals(1, groupNames.size());
    Assert.assertEquals("test.group", groupNames.get(0));

    uri = "/123/";
    try {
      groupNames = uriProcessor.getGroupNames(uri);
      Assert.fail("Should have fail");
    } catch (final Exception e) {}
  }
}
