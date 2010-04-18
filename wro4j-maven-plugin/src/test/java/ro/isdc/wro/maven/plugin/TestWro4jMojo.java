/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;


/**
 * Test class for {@link Wro4jMojo}
 *
 * @author Alex Objelean
 */
public class TestWro4jMojo {
  private Wro4jMojo mojo;
  private File destinationFolder;


  @Before
  public void setUp()
    throws Exception {
    mojo = new Wro4jMojo();
    final URL url = getClass().getClassLoader().getResource("wro.xml");
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setTargetGroups("g1");
    mojo.setContextFolder(wroFile.getParentFile().getParentFile());
    destinationFolder = new File("wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
    mojo.setDestinationFolder(destinationFolder);
  }


  @Test
  public void testMojoWithPropertiesSet()
    throws Exception {
    mojo.execute();
  }

  public static final class ExceptionThrowingWroManagerFactory
    extends DefaultMavenContextAwareManagerFactory {
    @Override
    protected void configureProcessors(final GroupsProcessor groupsProcessor) {
      final ResourcePostProcessor postProcessor = Mockito.mock(ResourcePostProcessor.class);
      try {
        Mockito.doThrow(new RuntimeException()).when(postProcessor).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
      } catch (final IOException e) {
        Assert.fail("never happen");
      }
      groupsProcessor.addPostProcessor(postProcessor);
    }
  }


  @Test(expected=MojoExecutionException.class)
  public void testMojoWithWroManagerFactorySet()
    throws Exception {
    mojo.setWroManagerFactory(ExceptionThrowingWroManagerFactory.class.getName());
    mojo.execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void testInvalidMojoWithWroManagerFactorySet()
    throws Exception {
    mojo.setWroManagerFactory("INVALID_CLASS_NAME");
    mojo.execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void testNoGroupSet()
    throws Exception {
    mojo.setTargetGroups(null);
    mojo.execute();
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(destinationFolder);
    } catch (final Exception e) {
    }
  }
}
