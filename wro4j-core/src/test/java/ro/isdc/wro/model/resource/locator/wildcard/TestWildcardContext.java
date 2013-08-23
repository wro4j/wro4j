/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestWildcardContext {
  @Test
  public void shouldCreateContextWithNullArguments() {
    final WildcardContext context = new WildcardContext(null, null);
    assertNull(context.getUri());
    assertNull(context.getFolder());
    assertNull(context.getWildcard());
  }

  @Test
  public void shouldCreateContextWithNotNullArguments() {
    final WildcardContext context = new WildcardContext("path/to/uri", new File("/path/to/folder/"));
    assertEquals(FilenameUtils.separatorsToSystem("path/to/uri"), FilenameUtils.separatorsToSystem(context.getUri()));
    assertEquals(FilenameUtils.separatorsToSystem("/path/to/folder"), context.getFolder().getPath());
    assertEquals("uri", context.getWildcard());
  }
}
