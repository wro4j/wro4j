/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.support.hash;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for {@link HashStrategy}
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class TestHashStrategy {
  private HashStrategy fingerprintCreator;

  @Test
  public void testMD5() throws Exception {
    final String input = "testString";
    fingerprintCreator = new MD5HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("536788f4dbdffeecfbb8f350a941eea3", hash);
  }

  @Test
  public void testSHA1() throws Exception {
    final String input = "testString";
    fingerprintCreator = new SHA1HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("956265657d0b637ef65b9b59f9f858eecf55ed6a", hash);
  }

  @Test
  public void testCRC32() throws Exception {
    final String input = "testString";
    fingerprintCreator = new CRC32HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("18f4fd08", hash);
  }
}
