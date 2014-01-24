/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * TestGroupsExtractor.
 * 
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestDefaultGroupExtractor {
  private DefaultGroupExtractor victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void init() {
    victim = new DefaultGroupExtractor();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotExtractResourceTypeUsingNullUri() {
    victim.getResourceType(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotExtractGroupNamesUsingNullUri() {
    victim.getGroupName(null);
  }
  
  @Test
  public void testExtractInvalidResourceType() {
    String uri = "/test.js";
    ResourceType type = victim.getResourceType(mockRequestForUri(uri));
    Assert.assertEquals(ResourceType.JS, type);
    
    uri = "/test.css";
    type = victim.getResourceType(mockRequestForUri(uri));
    Assert.assertEquals(ResourceType.CSS, type);
    
    uri = "/test.txt";
    Assert.assertNull(victim.getResourceType(mockRequestForUri(uri)));
  }
  
  @Test
  public void testExtractNoGroupName() {
    String groupName = victim.getGroupName(mockRequestForUri("/app/test.js"));
    Assert.assertEquals("test", groupName);
    
    groupName = victim.getGroupName(mockRequestForUri("/app/test.group.js"));
    Assert.assertEquals("test.group", groupName);
    
    Assert.assertEquals(null, victim.getGroupName(mockRequestForUri("/123/")));
  }
  
  private HttpServletRequest mockRequestForUri(final String uri) {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn(uri);
    return request;
  }
}
