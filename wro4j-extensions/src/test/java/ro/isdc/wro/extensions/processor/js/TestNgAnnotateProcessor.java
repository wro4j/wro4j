package ro.isdc.wro.extensions.processor.js;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.exec.ExecuteException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestNgAnnotateProcessor {
  private ResourcePostProcessor victim;
  private static boolean isSupported = false;


  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @BeforeClass
  public static void beforeClass() {
    isSupported = new NgAnnotateProcessor().isSupported();
  }

  /**
   * Checks if the test can be run by inspecting {@link NgAnnotateProcessor#isSupported()}
   */
  @Before
  public void beforeMethod() {
    victim = new NgAnnotateProcessor();
    Context.set(Context.standaloneContext());
    assumeTrue(isSupported);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldProcessInvalidJsUnchanged()
      throws Exception {
    final String invalidJs = "qwertwi42o";
    final StringWriter writer = new StringWriter();
    victim.process(new StringReader(invalidJs), writer);
    assertEquals(invalidJs, writer.toString());
  }

  @Test
  public void shouldSupportProcessorNgMinInstalled() {
    victim = new NgAnnotateProcessor() {
      @Override
      void doProcess(final InputStream in, final OutputStream out)
          throws ExecuteException, IOException {
        super.doProcess(in, out);
      }
    };
    assertTrue(((NgAnnotateProcessor) victim).isSupported());
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("../ngannotate");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", victim);
  }
}
