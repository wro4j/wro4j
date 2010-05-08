/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestDefaultWildcardStreamLocator {
  private WildcardStreamLocator locator;

  @Before
  public void setUp() {
    locator = new DefaultWildcardStreamLocator();
  }

  @Test
  public void testNoWildcardPreset1() {
    Assert.assertFalse(locator.hasWildcard("test/resource.css"));
  }

  @Test
  public void testNoWildcardPreset2() {
    Assert.assertFalse(locator.hasWildcard("test/resource[a].css"));
  }

  @Test
  public void testWildcardPreset1() {
    Assert.assertTrue(locator.hasWildcard("test/*.css"));
  }

  @Test
  public void testWildcardPreset2() {
    Assert.assertTrue(locator.hasWildcard("test/test.?ss"));
  }

  @Test
  public void testWildcardPreset3() {
    Assert.assertTrue(locator.hasWildcard("test/**.???"));
  }

  @Test(expected=IOException.class)
  public void cannotPassNullArgument() throws IOException {
    locator.locateStream(null, null);
  }

  @Test(expected=IOException.class)
  public void cannotPassNullFolder() throws IOException {
    locator.locateStream("/resource/*.css", null);
  }

  @Test
  public void testWithValidFolder() throws IOException {
    final File folder = new File(ClassLoader.getSystemResource("").getFile());
    locator.locateStream("/resource/*.css", folder);
  }

  @Test(expected=IOException.class)
  public void testWithInvalidFolder() throws IOException {
    final File folder = new File(ClassLoader.getSystemResource("1.css").getFile());
    locator.locateStream("/resource/*.css", folder);
  }
}
