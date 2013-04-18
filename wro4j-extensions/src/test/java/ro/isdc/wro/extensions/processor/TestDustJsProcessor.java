package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Test Dust.js processor.
 *
 * @author Eivind B Waaler
 */
public class TestDustJsProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    processor = new DustJsProcessor();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testSimpleString() throws Exception {
    final StringWriter writer = new StringWriter();
    processor.process(null, new StringReader("Hello {name}!"), writer);
    final String result = writer.toString();
    assertTrue(result.matches("\\(function\\(\\)\\{.*\\}\\)\\(\\);"));
  }

  @Test
  public void shouldTransformFilesFromFolder() throws IOException {
    final URL url = getClass().getResource("dustjs");
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");

    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }


  @Test
  public void shouldBeThreadSafe() throws Exception {
    final DustJsProcessor processor = new DustJsProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(null, new StringReader("Hello {name}!"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.JS);
  }
}
