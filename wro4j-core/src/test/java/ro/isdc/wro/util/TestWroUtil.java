/**
 *
 */
package ro.isdc.wro.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test {@link WroUtil} class.
 * @author Alex Objelean
 */
public class TestWroUtil {
  @Test(expected=IllegalArgumentException.class)
  public void cannotComputeEmptyLocation() {
    WroUtil.getPathInfoFromLocation("");
  }
  @Test
  public void computePathFromSomeLocation() {
    final String result = WroUtil.getPathInfoFromLocation("location");
    Assert.assertEquals("", result);
  }
  @Test
  public void computePathFromNestedLocation() {
    final String result = WroUtil.getPathInfoFromLocation("/a/b/c/d");
    Assert.assertEquals("/b/c/d", result);
  }

  @Test
  public void first() {
    final String result = WroUtil.getServletPathFromLocation("/a/b/c/d");
    Assert.assertEquals("/a", result);
  }
}
