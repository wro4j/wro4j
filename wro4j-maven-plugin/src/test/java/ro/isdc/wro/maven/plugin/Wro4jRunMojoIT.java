/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.mockito.Mockito;


/**
 * Integration test for wro4j mojo.
 *
 * @author Alex Objelean
 */
public class Wro4jRunMojoIT
    extends AbstractMojoTestCase {
  private File destinationFolder;
  private Wro4jMojo mojo;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp()
      throws Exception {
    super.setUp();
    final File testPom = new File(getBasedir(), "/src/test/resources/unit/1/pom.xml");
    mojo = (Wro4jMojo) lookupMojo("run", testPom);
    mojo.setIgnoreMissingResources(false);
  }

  /**
   * @throws Exception
   */
  public void testMojoGoal()
      throws Exception {
    destinationFolder = new File("wroTemp-" + new Date().getTime());
    mojo.setDestinationFolder(destinationFolder);

    mojo.setMavenProject(Mockito.mock(MavenProject.class));

    final URL url = getClass().getClassLoader().getResource("unit/1/src/main/webapp/WEB-INF/wro.xml");
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setContextFolder(wroFile.getParentFile().getParentFile());

    mojo.execute();
    assertNotNull(mojo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown()
      throws Exception {
    super.tearDown();
    FileUtils.deleteDirectory(destinationFolder);
  }
}
