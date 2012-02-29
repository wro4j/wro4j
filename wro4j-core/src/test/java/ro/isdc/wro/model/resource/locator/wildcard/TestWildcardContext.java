/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;

import junit.framework.Assert;

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
    Assert.assertEquals("path/to/uri", context.getUri());
    Assert.assertEquals("/path/to/folder", context.getFolder().getPath());
    Assert.assertEquals("uri", context.getWildcard());
  }
}
