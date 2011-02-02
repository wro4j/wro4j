/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.io.File;

import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestWro4jCommandLineRunner {
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
    System.out.println(wroFile);
    final String[] args = new String[] {"-m --wroFile " + wroFile};
    Wro4jCommandLineRunner.main(args);
  }
}
