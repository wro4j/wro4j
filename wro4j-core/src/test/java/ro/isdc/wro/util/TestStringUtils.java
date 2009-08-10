/**
 *
 */
package ro.isdc.wro.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test for {@link StringUtils} class.
 * @author Alex Objelean
 */
public class TestStringUtils {
  @Test
  public void testCleanPath() {
    final String result = StringUtils.cleanPath("/a/b/../d.txt");
    Assert.assertEquals("/a/d.txt", result);
  }
  @Test
  public void computeFileName() {
    final String result = StringUtils.getFilename("/a/b/../d.txt");
    Assert.assertEquals("d.txt", result);
  }
  @Test
  public void computeFileNameExtension() {
    final String result = StringUtils.getFilenameExtension("/a/b/../d.jpg");
    Assert.assertEquals("jpg", result);
  }
}
