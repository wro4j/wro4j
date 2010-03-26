/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;


/**
 * Test case for Wro4jMojo
 *
 * @author Alex Objelean
 */
public class Wro4jRunTest extends AbstractMojoTestCase {
  @Test
  public void testMojoGoal()
    throws Exception {
    runMojo("src/test/resources/unit/basic-test/pom.xml");
  }

  public void testIncompleteConfigurationMojoGoal()
    throws Exception {
    try {
      runMojo("src/test/resources/unit/basic-test/pom-incompleteConfiguration.xml");
      fail("should have thrown an exception");
    } catch (final MojoExecutionException e) {
      // TODO: handle exception
    }
  }

  private void runMojo(final String pomPath)
    throws Exception, MojoExecutionException {
    final File testPom = new File(getBasedir(), pomPath);
    final Wro4jMojo mojo = (Wro4jMojo)lookupMojo("run", testPom);
    mojo.execute();
  }
}
