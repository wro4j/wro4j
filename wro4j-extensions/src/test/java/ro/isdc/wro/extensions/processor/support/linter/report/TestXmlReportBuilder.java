package ro.isdc.wro.extensions.processor.support.linter.report;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.ResourceLinterErrors;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


/**
 * @author Alex Objelean
 */
public class TestXmlReportBuilder {
  @Test(expected = NullPointerException.class)
  public void cannotCreateBuilderWithNullErrors() {
    XmlReportBuilder.create(null);
  }
  
  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("xml");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "*", new ResourcePreProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final Type type = new TypeToken<List<ResourceLinterErrors<LinterError>>>() {}.getType();
        final List<ResourceLinterErrors<LinterError>> errors = new Gson().fromJson(reader, type);
        XmlReportBuilder.create(errors).write(new WriterOutputStream(writer));
      }
    });
  }
  
  public static void main(String[] args) {
    final List<ResourceLinterErrors<LinterError>> errors = new ArrayList<ResourceLinterErrors<LinterError>>();
    Collection<LinterError> file1Errors = new ArrayList<LinterError>();
    LinterError linterError = new LinterError();
    linterError.setCharacter(2);
    linterError.setEvidence("evidence");
    linterError.setLine(20);
    linterError.setReason("reason");
    file1Errors.add(linterError);
    errors.add(ResourceLinterErrors.<LinterError> create("file1", file1Errors));
    final Type type = new TypeToken<List<ResourceLinterErrors<LinterError>>>() {}.getType();
    String json = new GsonBuilder().setPrettyPrinting().create().toJson(errors, type);
    System.out.println(json);
  }
}
