/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;

/**
 * @author Alex Objelean
 */
public class TestWro4jCommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(TestWro4jCommandLineRunner.class);
  private File destinationFolder;

  @Before
  public void setUp() {
    destinationFolder = new File("wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(destinationFolder);
  }

  @Test
  public void processWrongArgument() throws Exception {
    final String[] args = new String[] {"-wrongArgument"};
    Wro4jCommandLineRunner.main(args);
  }

  @Test
  public void processNoArguments() throws Exception {
    Wro4jCommandLineRunner.main("".split(" "));
  }

  @Test
  public void processCorrectArguments() throws Exception {
    Wro4jCommandLineRunner.main("-m".split(" "));
  }

  private void invokeRunner(final String[] args) throws Exception {
    new Wro4jCommandLineRunner() {
      @Override
      public void doMain(final String[] arguments) {
        super.doMain(arguments);
      }
      @Override
      protected void onException(final Exception e) {
        LOG.error("Exception occured: ", e.getCause());
        throw new RuntimeException(e);
      }
    }.doMain(args);
  }

  @Test
  public void useSeveralProcessors() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + "\\wro.xml";
    final String processorsList = CssLintProcessor.ALIAS + "," + JsHintProcessor.ALIAS;
    final String[] args = String.format("--wroFile %s --contextFolder %s --destinationFolder %s -m -c " + processorsList,
        new Object[] {
          wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }

  @Test
  public void useCssLint() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + "\\wro.xml";

    final String[] args = String.format("--wroFile %s --contextFolder %s --destinationFolder %s -m -c " + CssLintProcessor.ALIAS,
        new Object[] {
          wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }


  @Test
  public void useJsHint()
      throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + "\\wro.xml";

    final String[] args = String.format(
        "--wroFile %s --contextFolder %s --destinationFolder %s -m -c " + JsHintProcessor.ALIAS, new Object[] {
          wroFile, contextFolder, destinationFolder.getAbsolutePath()
        }).split(" ");
    invokeRunner(args);
  }

  @Test
  public void processTestWroXml()
      throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + "\\wro.xml";

    LOG.debug(wroFile);
    final String[] args = String.format("-m --wroFile %s --contextFolder %s --destinationFolder %s", new Object[] {
      wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }
}
