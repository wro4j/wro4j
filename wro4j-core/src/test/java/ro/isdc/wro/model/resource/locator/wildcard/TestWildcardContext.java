/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestWildcardContext {
  @Test
  public void shouldCreateContextWithNullArguments() {
    final WildcardContext context = new WildcardContext(null, null);
    Assert.assertNull(context.getUri());
    Assert.assertNull(context.getFolder());
    Assert.assertNull(context.getWildcard());
  }

  @Test
  public void shouldCreateContextWithNotNullArguments() {
    final WildcardContext context = new WildcardContext("path/to/uri", new File("/path/to/folder/"));
    Assert.assertEquals(FilenameUtils.separatorsToSystem("path/to/uri"), FilenameUtils.separatorsToSystem(context.getUri()));
    Assert.assertEquals(FilenameUtils.separatorsToSystem("/path/to/folder"), context.getFolder().getPath());
    Assert.assertEquals("uri", context.getWildcard());
  }
}
