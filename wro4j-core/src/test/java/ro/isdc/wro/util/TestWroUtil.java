/**
 *
 */
package ro.isdc.wro.util;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.http.HttpHeader;

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
  public void testGetFolderForUri() {
    Assert.assertEquals("a/b/c/", WroUtil.getFolderOfUri("a/b/c/d"));
    Assert.assertEquals("", WroUtil.getFolderOfUri("a.css"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAddGzipHeaderWithNullResponse() {
    WroUtil.addGzipHeader(null);
  }

  @Test(expected=WroRuntimeException.class)
  public void testAddGzipHeaderWhenGzipIsNotSupported() {
    WroUtil.addGzipHeader(Mockito.mock(HttpServletResponse.class));
  }

  @Test
  public void testAddGzipHeader() {
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(response.containsHeader(HttpHeader.CONTENT_ENCODING.toString())).thenReturn(true);
    WroUtil.addGzipHeader(response);
  }
}
