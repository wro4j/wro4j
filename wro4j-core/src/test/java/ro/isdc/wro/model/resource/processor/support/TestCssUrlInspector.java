package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.CssUrlInspector.ItemHandler;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestCssUrlInspector {
  private CssUrlInspector victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    victim = new CssUrlInspector();
  }

  @Test
  public void shouldRemoveOriginalUrl()
      throws Exception {
    compareResultsFromFolderUsingProcessor("expectedEmptyReplace", createProcessorWithHandler(new ItemHandler() {
      public String replace(final String originalDeclaration, final String originalUrl) {
        return originalDeclaration.replace(originalUrl, "");
      }
    }));
  }

  private void compareResultsFromFolderUsingProcessor(final String expectedFolderName,
      final ResourcePreProcessor processor)
      throws Exception {
    final URL url = getClass().getResource("cssurlinspector");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), expectedFolderName);
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  private ResourcePreProcessor createProcessorWithHandler(final ItemHandler handler) {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        writer.write(victim.findAndReplace(IOUtils.toString(reader), handler));
      }
    };
  }
}
