/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Objelean
 */
public class TestWro4jCommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(TestWro4jCommandLineRunner.class);

  @Before
  public void startUp() {
    LOG.debug("startup");
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
    final String wroFile = new File(getClass().getResource("").getFile()).getAbsolutePath() + "\\wro.xml";
    LOG.debug(wroFile);
    final String[] args = new String("-m --wroFile " + wroFile).split(" ");
    Wro4jCommandLineRunner.main(args);
  }
}
