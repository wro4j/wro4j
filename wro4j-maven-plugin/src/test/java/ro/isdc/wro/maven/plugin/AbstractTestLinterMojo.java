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

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * Test class for {@link JsHintMojo}
 *
 * @author Alex Objelean
 */
public abstract class AbstractTestLinterMojo {
  private AbstractSingleProcessorMojo mojo;

  @Before
  public void setUp()
      throws Exception {
    mojo = newLinterMojo();
    mojo.setIgnoreMissingResources(false);
    setWroWithValidResources();
    mojo.setTargetGroups("g1");
    mojo.setMavenProject(Mockito.mock(MavenProject.class));
  }

  protected final AbstractSingleProcessorMojo getMojo() {
    return mojo;
  }

  /**
   * @return Mojo to test.
   */
  protected abstract AbstractSingleProcessorMojo newLinterMojo();

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

  protected final void setWroWithInvalidResources()
      throws Exception {
    setWroFile("wroWithInvalidResources.xml");
  }

  @Test(expected = MojoExecutionException.class)
  public void cannotExecuteWhenInvalidResourcesPresentAndDoNotIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    mojo.setIgnoreMissingResources(false);
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testResourceWithErrors()
      throws Exception {
    mojo.setTargetGroups("invalid");
    mojo.execute();
  }

  @Test(expected = MojoExecutionException.class)
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

  @Test(expected = CustomException.class)
  public void shouldOverrideCustomProcessorsFactory()
      throws Throwable {
    try {
      mojo.setWroManagerFactory(CustomWroManagerFactory.class.getName());
      mojo.setTargetGroups(null);
      mojo.execute();
    } catch (final MojoExecutionException e) {
      throw e.getCause();
    }
  }

  private static class CustomException
      extends WroRuntimeException {
    public CustomException(final String message) {
      super(message);
    }
  }

  public static class CustomWroManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      throw new CustomException("Should have not call this method");
    }

    @Override
    protected WroModelFactory newModelFactory() {
      return new WroModelFactory() {
        public WroModel create() {
          return new WroModel().addGroup(new Group("all"));
        }

        public void destroy() {
        }
      };
    }
  }

}
