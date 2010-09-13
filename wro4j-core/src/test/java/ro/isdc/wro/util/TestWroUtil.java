/**
 *
 */
package ro.isdc.wro.util;

import java.io.InputStream;

import javax.servlet.ServletException;

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
  public void computeServletPathFromLocation() {
    final String result = WroUtil.getServletPathFromLocation("/a/b/c/d");
    Assert.assertEquals("/a", result);
  }

  @Test
  public void test1GetFilterPath() throws Exception {
    final InputStream is = ClassLoader.getSystemResourceAsStream(WroUtil.toPackageAsFolder(getClass()) + "/web.xml");
    Assert.assertEquals("wro/", WroUtil.getFilterPath("Test1", is));
  }

  @Test
  public void test2GetFilterPath() throws Exception {
    final InputStream is = ClassLoader.getSystemResourceAsStream(WroUtil.toPackageAsFolder(getClass()) + "/web.xml");
    Assert.assertEquals("", WroUtil.getFilterPath("Test2", is));
  }

  @Test(expected=ServletException.class)
  public void test3GetFilterPath() throws Exception {
    final InputStream is = ClassLoader.getSystemResourceAsStream(WroUtil.toPackageAsFolder(getClass()) + "/web.xml");
    Assert.assertEquals("wro/", WroUtil.getFilterPath("Test3", is));
  }
}
