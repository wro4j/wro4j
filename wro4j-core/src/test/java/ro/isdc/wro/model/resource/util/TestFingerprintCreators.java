/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.util;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for {@link MD5FingerprintCreator}
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class TestFingerprintCreators {
  private FingerprintCreator fingerprintCreator;

  @Test
  public void testMD5() throws Exception {
    final String input = "testString";
    fingerprintCreator = new MD5FingerprintCreator();
    final String hash = fingerprintCreator.create(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("536788f4dbdffeecfbb8f350a941eea3", hash);
  }


  @Test
  public void testCRC32() throws Exception {
    final String input = "testString";
    fingerprintCreator = new CRC32FingerprintCreator();
    final String hash = fingerprintCreator.create(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("18f4fd08", hash);
  }
}
