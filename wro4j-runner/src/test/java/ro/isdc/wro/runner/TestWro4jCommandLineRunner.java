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
    final String[] args = new String[] {};
    Wro4jCommandLineRunner.main(args);
  }

  @Test
  public void processCorrectArguments() throws Exception {
    final String[] args = new String[] {"-m"};
    Wro4jCommandLineRunner.main(args);
  }

  @Test
  public void processTestWroXml() throws Exception {
    final String contextFolder = new File(getClass().getResource("").getFile()).getAbsolutePath();
    final String wroFile = contextFolder + "\\wro.xml";

    LOG.debug(wroFile);
    final String[] args = String.format("-m --wroFile %s --contextFolder %s --destinationFolder %s",
      new Object[] { wroFile, contextFolder, destinationFolder.getAbsolutePath() }).split(" ");
    Wro4jCommandLineRunner.main(args);
  }
}
