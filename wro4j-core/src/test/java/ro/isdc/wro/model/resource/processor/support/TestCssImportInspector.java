package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestCssImportInspector {

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

  private void compareResultsFromFolderUsingProcessor(final String expectedFolderName, final ResourcePreProcessor processor) throws Exception {
    final URL url = getClass().getResource("cssimport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), expectedFolderName);
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldDetectImportStatement() {
    assertTrue(new CssImportInspector("@import 'style.css'").containsImport());
    assertTrue(new CssImportInspector("@import url(style.css)").containsImport());
  }

  @Test
  public void shouldDetectMissingImportStatement() {
    assertFalse(new CssImportInspector("#someId {color: red}").containsImport());
    assertFalse(new CssImportInspector("#import {display: block}").containsImport());
  }


  private ResourcePreProcessor createFindImportsProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final List<String> results = new CssImportInspector(IOUtils.toString(reader)).findImports();
        for (final String string : results) {
          writer.write(string + "\n");
        }
      }
    };
  }

  private ResourcePreProcessor createImportsRemovalProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {

        writer.write(new CssImportInspector("").removeImportsFromComments(IOUtils.toString(reader)));
      }
    };
  }

  private ResourcePreProcessor createRemoveImportsProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        writer.write(new CssImportInspector(IOUtils.toString(reader)).removeImportStatements());
      }
    };
  }
}
