/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
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
    mojo.setIgnoreMissingResources(Boolean.FALSE.toString());
    mojo.setMinimize(true);
    mojo.setBuildDirectory(new File(getBasedir()));
  }

  /**
   * @throws Exception
   */
  public void testMojoGoal()
      throws Exception {
    destinationFolder = new File("target/wroTemp-" + new Date().getTime());
    mojo.setDestinationFolder(destinationFolder);

    MavenProject mockMavenProject = Mockito.mock(MavenProject.class);
    Model mockMavenModel = Mockito.mock(Model.class);
    Build mockBuild = Mockito.mock(Build.class);
    Mockito.when(mockMavenProject.getModel()).thenReturn(mockMavenModel);
    Mockito.when(mockMavenModel.getBuild()).thenReturn(mockBuild);
    Mockito.when(mockBuild.getDirectory()).thenReturn(FileUtils.getTempDirectoryPath());
    
    mojo.setMavenProject(mockMavenProject);

    final URL url = getClass().getClassLoader().getResource("unit/1/src/main/webapp/WEB-INF/wro.xml");
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setContextFolder(wroFile.getParentFile().getParentFile().getPath());

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
