/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.concurrent.TaskExecutor;


/**
 * Test class for {@link JsHintMojo}
 *
 * @author Alex Objelean
 */
public abstract class AbstractTestLinterMojo {
  private AbstractLinterMojo<?> mojo;

  @Before
  public void setUp()
      throws Exception {
    mojo = newLinterMojo();
    initializeMojo(mojo);
  }

  /**
   * perform default initialization of provided mojo.
   */
  private void initializeMojo(final AbstractLinterMojo<?> mojo)
      throws Exception {
    mojo.setLog(new SilentLog());
    mojo.setIgnoreMissingResources(Boolean.FALSE.toString());
    setWroWithValidResources();
    mojo.setTargetGroups("g1");
    MavenProject mockMavenProject = Mockito.mock(MavenProject.class);
    Model mockMavenModel = Mockito.mock(Model.class);
    Build mockBuild = Mockito.mock(Build.class);
    Mockito.when(mockMavenProject.getModel()).thenReturn(mockMavenModel);
    Mockito.when(mockMavenModel.getBuild()).thenReturn(mockBuild);
    Mockito.when(mockBuild.getDirectory()).thenReturn(FileUtils.getTempDirectoryPath());
    mojo.setMavenProject(mockMavenProject);
  }

  protected final AbstractLinterMojo<?> getMojo() {
    return mojo;
  }

  /**
   * @return Mojo to test.
   */
  protected abstract AbstractLinterMojo<?> newLinterMojo();

  private void setWroFile(final String classpathResourceName)
      throws URISyntaxException {
    final URL url = getClass().getClassLoader().getResource(classpathResourceName);
    final File wroFile = new File(url.toURI());
    mojo.setWroFile(wroFile);
    mojo.setContextFolder(wroFile.getParentFile().getParentFile().getPath());
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

  @Test
  public void shouldUseTaskExecutorWhenRunningInParallel()
      throws Exception {
    final AtomicBoolean invoked = new AtomicBoolean();
    final TaskExecutor<Void> taskExecutor = new TaskExecutor<Void>() {
      @Override
      public void submit(final Collection<Callable<Void>> callables)
          throws Exception {
        invoked.set(true);
        super.submit(callables);
      }
    };
    mojo.setFailNever(true);
    mojo.setTaskExecutor(taskExecutor);
    mojo.setIgnoreMissingResources(Boolean.TRUE.toString());

    mojo.setParallelProcessing(false);
    mojo.execute();
    assertFalse(invoked.get());

    mojo.setParallelProcessing(true);
    mojo.execute();
    assertTrue(invoked.get());
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

  @After
  public void tearDown() {
    getMojo().clean();
  }
}
