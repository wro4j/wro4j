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
}
