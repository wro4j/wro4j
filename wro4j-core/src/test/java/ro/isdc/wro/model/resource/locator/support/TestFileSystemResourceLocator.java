/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ResourceLocator;


/**
 * Tests if {@link FileSystemResourceLocator} works properly.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestFileSystemResourceLocator {
  /**
   * UriLocator to test.
   */
  private FileSystemResourceLocator locator;


  /**
   * Create path to the file relative to this class.
   *
   * @param location
   */
  private File createFile(final String location) {
    return new File(getClass().getResource(location.trim()).getPath());
  }


  @Test
  public void resourceAvailable()
    throws IOException {
    locator = new FileSystemResourceLocator(createFile("test.css"));
    Assert.assertNotNull(locator.getInputStream());
  }


  @Test
  public void testRelative()
    throws Exception {
    locator = new FileSystemResourceLocator(createFile("test.css"));

    ResourceLocator relative = locator.createRelative("1.css");
    Assert.assertNotNull(relative.getInputStream());

    relative = locator.createRelative("../support/1.css");
    Assert.assertNotNull(relative.getInputStream());
  }

  @Test(expected=FileNotFoundException.class)
  public void testInvalidRelative()
    throws Exception {
    locator = new FileSystemResourceLocator(createFile("test.css"));

    ResourceLocator relative = locator.createRelative("1.css");
    Assert.assertNotNull(relative.getInputStream());

    relative = locator.createRelative("invalidFolder/../support/1.css");
    Assert.assertNotNull(relative.getInputStream());
  }


  @Test
  public void resourceAvailableWithTrailingSpaces()
    throws IOException {
    locator = new FileSystemResourceLocator(createFile("  test.css  "));
    Assert.assertNotNull(locator.getInputStream());
    Assert.assertNotSame(0, locator.lastModified());
  }


  @Test(expected=FileNotFoundException.class)
  public void testWildcardResources()
    throws IOException {
    locator = new FileSystemResourceLocator(new File("*.css"));
    Assert.assertNotNull(locator.getInputStream());
    Assert.assertEquals(0, locator.lastModified());
  }
}
