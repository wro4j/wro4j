/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * Test class for {@link JsHintMojo}
 *
 * @author Alex Objelean
 */
public class TestJsHintMojo {
  private JsHintMojo mojo;

  @Before
  public void setUp()
    throws Exception {
    mojo = new JsHintMojo();
    mojo.setIgnoreMissingResources(false);
    setWroWithValidResources();
    mojo.setTargetGroups("g1");
    mojo.setMavenProject(Mockito.mock(MavenProject.class));
  }


  private void setWroFile(final String classpathResourceName)
    throws URISyntaxException {
    final URL url = getClass().getClassLoader().getResource(classpathResourceName);
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setContextFolder(wroFile.getParentFile().getParentFile());
  }

  private void setWroWithValidResources() throws Exception {
    setWroFile("wro.xml");
  }

  private void setWroWithInvalidResources() throws Exception {
    setWroFile("wroWithInvalidResources.xml");
  }

  @Test
  public void testMojoWithPropertiesSet()
    throws Exception {
    mojo.setIgnoreMissingResources(true);
    mojo.execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void cannotExecuteWhenInvalidResourcesPresentAndDoNotIgnoreMissingResources() throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(false);
    mojo.execute();
  }

  @Test
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue() throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(true);
    mojo.execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void testResourceWithErrors()
    throws Exception {
    mojo.setTargetGroups("invalid");
    mojo.execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void testResourceWithUndefVariablesAndUndefOption()
    throws Exception {
    mojo.setOptions("undef, browser");
    mojo.setTargetGroups("undef");
    mojo.execute();
  }

  @Test
  public void testErrorsWithNoFailFast()
    throws Exception {
    mojo.setFailNever(true);
    mojo.setOptions("undef, browser");
    mojo.setTargetGroups("undef");
    mojo.execute();
  }

  @Test
  public void testResourceWithUndefVariables()
    throws Exception {
    mojo.setTargetGroups("undef");
    mojo.execute();
  }

  @Test
  public void testEmptyOptions()
    throws Exception {
    mojo.setOptions("");
    mojo.setTargetGroups("undef");
    mojo.execute();
  }

  @Test
  public void shouldOverrideCustomProcessorsFactory()
    throws Exception {
    mojo.setWroManagerFactory(CustomWroManagerFactory.class.getName());
    mojo.execute();
  }

  public static class CustomWroManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      return new SimpleProcessorsFactory().addPreProcessor(new JSMinProcessor());
    }

    @Override
    protected WroModelFactory newModelFactory() {
      return super.newModelFactory();
    }
  }

}
