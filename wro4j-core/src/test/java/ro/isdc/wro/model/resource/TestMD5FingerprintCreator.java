/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link MD5FingerprintCreator}
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class TestMD5FingerprintCreator {
  private FingerprintCreator fingerprintCreator;

  @Before
  public void setUp() {
    fingerprintCreator = new MD5FingerprintCreator();
  }

  @Test
  public void testHashForSampleInput() {
    final String input = "testString";
    final String hash = fingerprintCreator.create(new ByteArrayInputStream(input.getBytes()));
    Assert.assertEquals("391a41aa2e8a564787d6c2ef246abc71", hash);
  }
}
