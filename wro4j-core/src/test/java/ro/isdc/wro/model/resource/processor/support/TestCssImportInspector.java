package ro.isdc.wro.model.resource.processor.support;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestCssImportInspector {
  private CssImportInspector victim;

  @Before
  public void setUp() {
    victim = new CssImportInspector();
  }

  @Test
  public void shouldRemoveImportsFromComments() throws Exception {
    compareResultsFromFolderUsingProcessor("expectedRemoveImportsFromComments", createImportsRemovalProcessor());
  }

  @Test
  public void shouldRemoveImports() throws Exception {
    compareResultsFromFolderUsingProcessor("expectedRemoveImports", createRemoveImportsProcessor());
  }

  @Test
  public void shouldFindImports() throws Exception {
    compareResultsFromFolderUsingProcessor("expectedFindImports", createFindImportsProcessor());
  }

  private void compareResultsFromFolderUsingProcessor(final String expectedFolderName, final ResourceProcessor processor) throws Exception {
    final URL url = getClass().getResource("cssimport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), expectedFolderName);
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  private ResourceProcessor createFindImportsProcessor() {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final List<String> results = victim.findImports(IOUtils.toString(reader));
        for (final String string : results) {
          writer.write(string + "\n");
        }
      }
    };
  }

  private ResourceProcessor createImportsRemovalProcessor() {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        writer.write(victim.removeImportsFromComments(IOUtils.toString(reader)));
      }
    };
  }

  private ResourceProcessor createRemoveImportsProcessor() {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        writer.write(victim.removeImportStatements(IOUtils.toString(reader)));
      }
    };
  }
}
