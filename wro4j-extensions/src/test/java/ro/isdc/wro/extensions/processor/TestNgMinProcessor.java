package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.NgMinProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


public class TestNgMinProcessor {
  private ResourcePostProcessor victim;
  private static boolean isSupported = false;

  @BeforeClass
  public static void beforeClass() {
    isSupported = new NgMinProcessor().isSupported();
  }

  /**
   * Checks if the test can be run by inspecting {@link NgMinProcessor#isSupported()}
   */
  @Before
  public void beforeMethod() {
    victim = new NgMinProcessor();
    Context.set(Context.standaloneContext());
    assumeTrue(isSupported);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void shouldLeaveInvalidJsUnchanged()
      throws Exception {
    final String invalidJs = "al /ert- -- 1";
    final StringWriter writer = new StringWriter();
    victim.process(new StringReader(invalidJs), writer);
    assertEquals(invalidJs, writer.toString());
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("ngmin");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", victim);
  }
}
