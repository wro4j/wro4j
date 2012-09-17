package ro.isdc.wro.extensions.support.lint;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * @author Alex Objelean
 */
public class TestXmlLinterErrorReportFormatter {
  @Test(expected = NullPointerException.class)
  public void cannotCreateBuilderWithNullLintReport() {
    XmlLinterErrorReportFormatter.create(null, XmlLinterErrorReportFormatter.Type.LINT);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotCreateBuilderWithNullType() {
    XmlLinterErrorReportFormatter.create(new LintReport(), null);
  }
  
  @Test
  public void shouldFormatLintReportFromFolder()
      throws Exception {
    final URL url = getClass().getResource("formatter/xml/lint");
    checkFormattedRportsFromFolder(url, XmlLinterErrorReportFormatter.Type.LINT);
  }
  
  @Test
  public void shouldFormatCheckstypeReportFromFolder()
      throws Exception {
    final URL url = getClass().getResource("formatter/xml/checkstyle");
    checkFormattedRportsFromFolder(url, XmlLinterErrorReportFormatter.Type.CHECKSTYLE);
  }

  private void checkFormattedRportsFromFolder(final URL url, final XmlLinterErrorReportFormatter.Type formatterType)
      throws IOException {
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "*", new ResourcePreProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final Type type = new TypeToken<LintReport<LinterError>>() {}.getType();
        final LintReport<LinterError> errors = new Gson().fromJson(reader, type);
        XmlLinterErrorReportFormatter.create(errors, formatterType).write(
            new WriterOutputStream(writer));
      }
    });
  }
}
