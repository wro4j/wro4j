package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestCssImportInspector {
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Test
  public void shouldRemoveImportsFromComments()
      throws Exception {
    compareResultsFromFolderUsingProcessor("expectedRemoveImportsFromComments", createImportsRemovalProcessor());
  }

  @Test
  public void shouldRemoveImports()
      throws Exception {
    compareResultsFromFolderUsingProcessor("expectedRemoveImports", createRemoveImportsProcessor());
  }

  @Test
  public void shouldFindImports()
      throws Exception {
    compareResultsFromFolderUsingProcessor("expectedFindImports", createFindImportsProcessor());
  }

  private void compareResultsFromFolderUsingProcessor(final String expectedFolderName,
      final ResourcePreProcessor processor)
      throws Exception {
    final URL url = getClass().getResource("cssimport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), expectedFolderName);
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldDetectImportStatement() {
    assertHasImport("@import 'style.css'");
    assertHasImport("@import url(style.css)");
  }

  @Test
  public void shouldDetectMissingImportStatement() {
    assertFalse(createCssImportInspector("#someId {color: red}").containsImport());
    assertFalse(createCssImportInspector("#import {display: block}").containsImport());
  }

  private ResourcePreProcessor createFindImportsProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final List<String> results = createCssImportInspector(IOUtils.toString(reader)).findImports();
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
        writer.write(createCssImportInspector(StringUtils.EMPTY).removeImportsFromComments(IOUtils.toString(reader)));
      }
    };
  }

  private ResourcePreProcessor createRemoveImportsProcessor() {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        writer.write(createCssImportInspector(IOUtils.toString(reader)).removeImportStatements());
      }
    };
  }

  protected CssImportInspector createCssImportInspector(final String cssContent) {
    return new CssImportInspector(cssContent);
  }

  protected final void assertHasImport(final String cssContent) {
    assertTrue(createCssImportInspector(cssContent).containsImport());
  }
}
