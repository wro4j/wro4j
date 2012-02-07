/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.runner;

import java.io.File;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroUtil;

/**
 * @author Alex Objelean
 */
public class TestWro4jCommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(TestWro4jCommandLineRunner.class);
  private File destinationFolder;

  @Before
  public void setUp() {
    destinationFolder = new File(FileUtils.getTempDirectory(), "wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(destinationFolder);
  }


  @Test
  public void processWrongArgument()
    throws Exception {
    try {
      final String[] args = new String[] { "-wrongArgument" };
      invokeRunner(args);
      Assert.fail("Should have failed!");
    } catch (final Exception e) {
      Assert.assertEquals(CmdLineException.class, e.getCause().getClass());
    }
  }

  @Test
  public void processNoArguments() throws Exception {
    try {
      invokeRunner("".split(" "));
    } catch (final Exception e) {
      Assert.assertEquals(CmdLineException.class, e.getCause().getClass());
    }
  }

  @Test
  public void processCorrectArguments() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + File.separator + "wro.xml";
    final String[] args = String.format(
      "--wroFile %s --contextFolder %s -m ",
        new Object[] {
          wroFile, contextFolder
    }).split(" ");
    invokeRunner(args);
  }

  private void invokeRunner(final String[] args) throws Exception {
    new Wro4jCommandLineRunner() {
      @Override
      protected void onException(final Exception e) {
        WroUtil.wrapWithWroRuntimeException(e);
      }
    }.doMain(args);
  }


  @Test
  public void cssUrlRewriterShouldWorkProperly() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();

    setDestinationFolder(new File(contextFolder, "targetCssFolder"));
    final String wroFile = contextFolder + File.separator + "wro.xml";
    LOG.debug("wroFile: {}", wroFile);
    final String processorsList = ConfigurableProcessorsFactory.createItemsAsString(CssUrlRewritingProcessor.ALIAS);
    final String[] args = String.format(
      "--wroFile %s --contextFolder %s --destinationFolder %s -m --preProcessors " + processorsList,
        new Object[] {
          wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }

  /**
   * Use this method for correct clean-up of resources.
   */
  private void setDestinationFolder(final File file) {
    FileUtils.deleteQuietly(destinationFolder);
    this.destinationFolder = file;
  }

  @Test
  public void useSeveralProcessors() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + File.separator + "wro.xml";
    LOG.debug("wroFile: {}", wroFile);
    final String processorsList = ConfigurableProcessorsFactory.createItemsAsString(CssMinProcessor.ALIAS,
      JSMinProcessor.ALIAS, CssUrlRewritingProcessor.ALIAS);
    final String[] args = String.format(
      "--wroFile %s --contextFolder %s --destinationFolder %s -m --preProcessors " + processorsList,
        new Object[] {
          wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }

  @Test
  public void useCssLint() throws Exception {
    try {
      final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
      final String wroFile = contextFolder + File.separator + "wro.xml";

      final String[] args = String.format(
        "--wroFile %s --contextFolder %s --destinationFolder %s -m -c " + CssLintProcessor.ALIAS,
        new Object[] { wroFile, contextFolder, destinationFolder.getAbsolutePath() }).split(" ");
      invokeRunner(args);
    } catch (final Exception e) {
      Assert.assertEquals(CssLintException.class, e.getCause().getClass());
    }
  }


  @Test
  public void useJsHint()
      throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + File.separator + "wro.xml";

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
    final String wroFile = contextFolder + File.separator + "wro.xml";

    LOG.debug(wroFile);
    final String[] args = String.format("-m --wroFile %s --contextFolder %s --destinationFolder %s", new Object[] {
      wroFile, contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");
    invokeRunner(args);
  }

  @Test
  public void shouldAcceptGroovyDSLUsingSmartModelFactory() {
    final File contextFolderFile = new File(getClass().getResource("").getFile(), "dsl");
    final String contextFolder = contextFolderFile.getAbsolutePath();
    //final String wroFile = contextFolder + File.separator + "wro.xml";

    //LOG.debug(wroFile);
    final String[] args = String.format("-m --contextFolder %s --destinationFolder %s", new Object[] {
      contextFolder, destinationFolder.getAbsolutePath()
    }).split(" ");

    //invoke runner
    new Wro4jCommandLineRunner() {
      {{
        setDestinationFolder(destinationFolder);
      }}
      @Override
      protected File newDefaultWroFile() {
        return new File(contextFolderFile, "wro.xml");
      }
      @Override
      protected void onException(final Exception e) {
        LOG.error("Exception occured: ", e.getCause());
        throw new RuntimeException(e);
      }
    }.doMain(args);
  }
}
