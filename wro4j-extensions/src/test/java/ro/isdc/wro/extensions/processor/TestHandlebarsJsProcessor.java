package ro.isdc.wro.extensions.processor;

import static junit.framework.Assert.assertTrue;

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
import ro.isdc.wro.extensions.processor.js.HandlebarsJsProcessor;
import ro.isdc.wro.extensions.processor.support.handlebarsjs.HandlebarsJs;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test Handlebars.js processor.
 *
 * @author heldeen
 */
public class TestHandlebarsJsProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    processor = new HandlebarsJsProcessor();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testSimpleString()
      throws Exception {
    StringWriter writer = new StringWriter();
    processor.process(null, new StringReader("Hello {name}!"), writer);
    String result = writer.toString();
    assertTrue(result.startsWith(HandlebarsJs.HANDLEBARS_JS_TEMPLATES_INIT));
    assertTrue(result.contains("return \"Hello {name}!\\n\";} );"));
  }

  @Test
  public void shouldTransformFilesFromFolder()
      throws IOException {
    final URL url = getClass().getResource("handlebarsjs");
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");

    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "handlebars", processor);
  }

  @Test
  public void shouldBeThreadSafe()
      throws Exception {
    final HandlebarsJsProcessor processor = new HandlebarsJsProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(new StringReader("Hello {name}!"), new StringWriter());
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
