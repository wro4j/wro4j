/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.*;
import java.net.URL;


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
  public void shouldHandleWrongCss()
      throws Exception {
    final ResourcePostProcessor processor = new ExceptionHandlingProcessorDecorator(new CssMinProcessor());
    
    final URL url = getClass().getResource("cssmin");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedInvalid");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  
  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new CssMinProcessor(), ResourceType.CSS);
  }

  @Test
  public void shouldNotRemoveLastCharWhenAddingSemiColon() throws IOException {
      ResourcePostProcessor processor = new CssMinProcessor();
      URL url = getClass().getResource("cssmin");

      File testFile = new File(url.getFile(), "test/addingSemi.css");
      File expectedFile = new File(url.getFile(), "expected/addingSemi.css");
      StringWriter minifiedWriter = new StringWriter();
      processor.process(new FileReader(testFile), minifiedWriter);

      String minified = minifiedWriter.toString();

      String expected = IOUtils.toString(new FileInputStream(expectedFile));
      WroTestUtils.compare(expected, minified);
  }
}
