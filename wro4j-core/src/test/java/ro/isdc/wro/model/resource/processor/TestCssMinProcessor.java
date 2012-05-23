/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestCssMinProcessor {
  @Test
  public void testFromFolder()
    throws Exception {
    final ResourcePostProcessor processor = new CssMinProcessor();

    final URL url = getClass().getResource("cssmin");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportOnlyCssResources() {
    Assert.assertTrue(Arrays.equals(new ResourceType[] {
      ResourceType.CSS
    }, new ProcessorDecorator(new CssMinProcessor()).getSupportedResourceTypes()));
  }
}
