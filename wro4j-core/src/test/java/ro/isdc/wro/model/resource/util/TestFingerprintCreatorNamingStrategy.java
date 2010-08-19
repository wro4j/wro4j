/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.util;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.util.FingerprintCreator;
import ro.isdc.wro.model.resource.util.FingerprintEncoderNamingStrategy;
import ro.isdc.wro.model.resource.util.NamingStrategy;

/**
 * Test class for {@link FingerprintCreatorNamingStrategy}
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class TestFingerprintCreatorNamingStrategy {
  private NamingStrategy namingStrategy;
  private static final String HASH = "HASH";
  @Before
  public void setUp() {
    namingStrategy = new FingerprintEncoderNamingStrategy() {
      @Override
      protected FingerprintCreator newFingerprintCreator() {
        return new FingerprintCreator() {
          public String create(final InputStream inputStream)
            throws IOException {
            return HASH;
          }
        };
      };
    };
  }

  @Test
  public void testWithExtension() throws Exception {
    //second argument doesn't matter.
    final String result = namingStrategy.rename("fileName.js", null);
    Assert.assertEquals("fileName-" + HASH + ".js", result);
  }

  @Test
  public void testNoExtension() throws Exception {
  //second argument doesn't matter.
    final String result = namingStrategy.rename("fileName", null);
    Assert.assertEquals("fileName-" + HASH, result);
  }
}
