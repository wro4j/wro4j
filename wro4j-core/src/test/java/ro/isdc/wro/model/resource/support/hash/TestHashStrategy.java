/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.support.hash;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * Test class for {@link HashStrategy}
 * 
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class TestHashStrategy {
  private HashStrategy fingerprintCreator;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Test
  public void testMD5()
      throws Exception {
    final String input = "testString";
    fingerprintCreator = new MD5HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    assertEquals("536788f4dbdffeecfbb8f350a941eea3", hash);
  }
  
  @Test
  public void testSHA1()
      throws Exception {
    final String input = "testString";
    fingerprintCreator = new SHA1HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    assertEquals("956265657d0b637ef65b9b59f9f858eecf55ed6a", hash);
  }
  
  @Test
  public void testCRC32()
      throws Exception {
    final String input = "testString";
    fingerprintCreator = new CRC32HashStrategy();
    final String hash = fingerprintCreator.getHash(new ByteArrayInputStream(input.getBytes()));
    assertEquals("18f4fd08", hash);
  }
}
