/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;


/**
 * Test class for {@link Wro4jMojo}
 *
 * @author Alex Objelean
 */
public class TestWro4jMojo {
  private Wro4jMojo mojo;
  private File cssDestinationFolder;
  private File jsDestinationFolder;
  private File destinationFolder;

  @Before
  public void setUp()
      throws Exception {
    Context.set(Context.standaloneContext());
    mojo = new Wro4jMojo();
    mojo.setIgnoreMissingResources(false);
    mojo.setMinimize(true);
    setWroWithValidResources();
    destinationFolder = new File("wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
    cssDestinationFolder = new File("wroTemp-css-" + new Date().getTime());
    destinationFolder.mkdir();
    jsDestinationFolder = new File("wroTemp-js-" + new Date().getTime());
    destinationFolder.mkdir();
    mojo.setDestinationFolder(destinationFolder);
    mojo.setMavenProject(Mockito.mock(MavenProject.class));
  }

  private void setWroFile(final String classpathResourceName)
      throws URISyntaxException {
    final URL url = getClass().getClassLoader().getResource(classpathResourceName);
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setContextFolder(wroFile.getParentFile().getParentFile());
  }

  private void setWroWithValidResources()
      throws Exception {
    setWroFile("wro.xml");
  }

  private void setWroWithInvalidResources()
      throws Exception {
    setWroFile("wroWithInvalidResources.xml");
  }

  @Test
  public void testMojoWithPropertiesSetAndOneTargetGroup()
      throws Exception {
    mojo.setTargetGroups("g1");
    mojo.setIgnoreMissingResources(true);
    mojo.execute();
  }

  @Test
  public void testMojoWithPropertiesSet()
      throws Exception {
    mojo.setIgnoreMissingResources(false);
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testNoDestinationFolderSet()
      throws Exception {
    mojo.setDestinationFolder(null);
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testOnlyCssDestinationFolderSet()
      throws Exception {
    mojo.setCssDestinationFolder(cssDestinationFolder);
    mojo.setDestinationFolder(null);
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testOnlyJsDestinationFolderSet()
      throws Exception {
    mojo.setJsDestinationFolder(jsDestinationFolder);
    mojo.setDestinationFolder(null);
    mojo.execute();
  }

  @Test
  public void testJsAndCssDestinationFolderSet()
      throws Exception {
    mojo.setJsDestinationFolder(jsDestinationFolder);
    mojo.setCssDestinationFolder(cssDestinationFolder);
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void cannotExecuteWhenInvalidResourcesPresentAndDoNotIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(false);
    mojo.execute();
  }

  @Test
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue()
      throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(true);
    mojo.execute();
  }

  public static final class ExceptionThrowingWroManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      final ResourcePreProcessor postProcessor = Mockito.mock(ResourcePreProcessor.class);
      try {
        Mockito.doThrow(new RuntimeException()).when(postProcessor).process(null, Mockito.any(Reader.class),
            Mockito.any(Writer.class));
      } catch (final IOException e) {
        Assert.fail("never happen");
      }
      factory.addPostProcessor(postProcessor);
      return factory;
    }
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithWroManagerFactorySet()
      throws Exception {
    mojo.setWroManagerFactory(ExceptionThrowingWroManagerFactory.class.getName());
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testInvalidMojoWithWroManagerFactorySet()
      throws Exception {
    mojo.setWroManagerFactory("INVALID_CLASS_NAME");
    mojo.execute();
  }

  @Test
  public void executeWithNullTargetGroupsProperty()
      throws Exception {
    mojo.setTargetGroups(null);
    mojo.execute();
  }

  public static class CustomManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithCustomManagerFactoryWithInvalidResourceAndNotIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(false);
    mojo.setWroManagerFactory(CustomManagerFactory.class.getName());
    mojo.execute();
  }

  @Test
  public void testMojoWithCustomManagerFactoryWithInvalidResourceAndIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(true);
    mojo.setWroManagerFactory(CustomManagerFactory.class.getName());
    mojo.execute();
  }

  @After
  public void tearDown()
      throws Exception {
    Context.unset();
    FileUtils.deleteDirectory(destinationFolder);
    FileUtils.deleteDirectory(cssDestinationFolder);
    FileUtils.deleteDirectory(jsDestinationFolder);
  }
}
