package ro.isdc.wro.extensions.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.HoganJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import static junit.framework.Assert.assertTrue;

/**
 * Test Hogan.js processor.
 *
 * @author Eivind B Waaler
 */
public class TestHoganJsProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    processor = new HoganJsProcessor();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testSimpleString() throws Exception {
    StringWriter writer = new StringWriter();
    processor.process(null, new StringReader("Hello {{name}}!"), writer);
    String result = writer.toString();
    assertTrue(result.matches("function.*name.*"));
  }

  @Test
  public void shouldTransformFilesFromFolder() throws IOException {
    final URL url = getClass().getResource("hoganjs");
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");

    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final HoganJsProcessor processor = new HoganJsProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(null, new StringReader("Hello {{name}}!"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }
}
