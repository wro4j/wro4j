/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager.Builder;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactoryDecorator;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.naming.ConfigurableNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.FolderHashEncoderNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test class for {@link Wro4jMojo}
 *
 * @author Alex Objelean
 */
public class TestWro4jMojo {
  private static final Logger LOG = LoggerFactory.getLogger(TestWro4jMojo.class);
  @Mock
  private BuildContext mockBuildContext;
  @Mock
  private HashStrategy mockHashStrategy;
  private UriLocatorFactory mockLocatorFactory;
  @Mock
  private UriLocator mockLocator;
  private File cssDestinationFolder;
  private File jsDestinationFolder;
  private File destinationFolder;
  private File extraConfigFile;
  private Wro4jMojo mojo;



  @Before
  public void setUp()
    throws Exception {
    MockitoAnnotations.initMocks(this);
    mockLocatorFactory = new UriLocatorFactory() {
      public InputStream locate(final String uri)
          throws IOException {
        return mockLocator.locate(uri);
      }

      public UriLocator getInstance(final String uri) {
        return mockLocator;
      }
    };
    Context.set(Context.standaloneContext());
    mojo = new Wro4jMojo();
    setUpMojo(mojo);
  }

  /**
   * Perform basic initialization with valid values of the provided mojo.
   */
  private void setUpMojo(final Wro4jMojo mojo)
      throws Exception {
    mojo.setIgnoreMissingResources(false);
    mojo.setMinimize(true);
    setWroWithValidResources();
    destinationFolder = new File(FileUtils.getTempDirectory(), "wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
    cssDestinationFolder = new File(FileUtils.getTempDirectory(), "wroTemp-css-" + new Date().getTime());
    destinationFolder.mkdir();
    jsDestinationFolder = new File(FileUtils.getTempDirectory(), "wroTemp-js-" + new Date().getTime());
    destinationFolder.mkdir();
    extraConfigFile = new File(FileUtils.getTempDirectory(), "extraConfig-" + new Date().getTime());
    extraConfigFile.createNewFile();
    mojo.setBuildDirectory(destinationFolder);
    mojo.setExtraConfigFile(extraConfigFile);
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


  @Test(expected = MojoExecutionException.class)
  public void shouldFailWhenInvalidResourcesAreUsed()
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
    mojo.setIgnoreMissingResources(true);
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
    mojo.setIgnoreMissingResources(true);
    mojo.setTargetGroups(null);
    mojo.execute();
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


  @Test(expected = MojoExecutionException.class)
  public void testMojoWithConfigurableWroManagerFactory()
    throws Exception {
    setWroWithValidResources();
    mojo.setIgnoreMissingResources(true);
    // by default a valid file is used, set null explicitly
    mojo.setExtraConfigFile(null);
    mojo.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    mojo.execute();
  }


  @Test
  public void testMojoWithConfigurableWroManagerFactoryWithValidAndEmptyConfigFileSet()
    throws Exception {
    setWroWithValidResources();
    mojo.setIgnoreMissingResources(true);
    mojo.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    mojo.execute();
  }


  @Test
  public void testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet()
    throws Exception {
    setWroWithValidResources();
    final String preProcessors = ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS + "=cssMin";
    FileUtils.write(extraConfigFile, preProcessors);

    mojo.setIgnoreMissingResources(true);
    mojo.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    mojo.execute();
  }


  @Test
  public void testComputedAggregatedFolder()
    throws Exception {
    setWroWithValidResources();
    mojo.setWroManagerFactory(CssUrlRewriterWroManagerFactory.class.getName());
    mojo.setIgnoreMissingResources(true);
    final File cssDestinationFolder = new File(this.destinationFolder, "subfolder");
    cssDestinationFolder.mkdir();
    mojo.setCssDestinationFolder(cssDestinationFolder);
    mojo.execute();
  }


  @Test(expected = MojoExecutionException.class)
  public void testMojoWithConfigurableWroManagerFactoryWithInvalidPreProcessor()
    throws Exception {
    setWroWithValidResources();
    final String preProcessors = ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS + "=INVALID";
    FileUtils.write(extraConfigFile, preProcessors);

    mojo.setIgnoreMissingResources(true);
    mojo.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    mojo.execute();
  }

  @Test
  public void shouldGenerateGroupMappingUsingNoOpNamingStrategy()
    throws Exception {
    setWroWithValidResources();

    final File groupNameMappingFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    mojo.setGroupNameMappingFile(groupNameMappingFile.getPath());
    mojo.setIgnoreMissingResources(true);
    mojo.execute();

    //verify
    final Properties groupNames = new Properties();
    groupNames.load(new FileInputStream(groupNameMappingFile));
    LOG.debug("groupNames: {}", groupNames);
    Assert.assertEquals("g1.js", groupNames.get("g1.js"));

    FileUtils.deleteQuietly(groupNameMappingFile);
  }


  @Test
  public void shouldGenerateGroupMappingUsingCustomNamingStrategy()
    throws Exception {
    setWroWithValidResources();

    final File groupNameMappingFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    mojo.setWroManagerFactory(CustomNamingStrategyWroManagerFactory.class.getName());
    mojo.setGroupNameMappingFile(groupNameMappingFile.getPath());
    mojo.setIgnoreMissingResources(true);
    mojo.execute();

    //verify
    final Properties groupNames = new Properties();
    groupNames.load(new FileInputStream(groupNameMappingFile));
    LOG.debug("groupNames: {}", groupNames);
    Assert.assertEquals(CustomNamingStrategyWroManagerFactory.PREFIX + "g1.js", groupNames.get("g1.js"));

    FileUtils.deleteQuietly(groupNameMappingFile);
  }

  @Test
  public void shouldUseConfiguredNamingStrategy()
    throws Exception {
    setWroWithValidResources();

    final File extraConfigFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    final Properties props = new Properties();
    //TODO create a properties builder
    props.setProperty(ConfigurableNamingStrategy.KEY, FolderHashEncoderNamingStrategy.ALIAS);
    props.list(new PrintStream(extraConfigFile));

    mojo.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    mojo.setExtraConfigFile(extraConfigFile);
    mojo.setIgnoreMissingResources(true);
    mojo.execute();

    FileUtils.deleteQuietly(extraConfigFile);
  }

  public static final class ExceptionThrowingWroManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      final ResourcePostProcessor postProcessor = Mockito.mock(ResourcePostProcessor.class);
      try {
        Mockito.doThrow(new RuntimeException()).when(postProcessor).process(Mockito.any(Reader.class),
          Mockito.any(Writer.class));
      } catch (final IOException e) {
        Assert.fail("never happen");
      }
      factory.addPostProcessor(postProcessor);
      return factory;
    }
  }


  public static class CustomManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  }


  public static final class CustomNamingStrategyWroManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
    public static final String PREFIX = "renamed";
    {
      setNamingStrategy(new NamingStrategy() {
        public String rename(final String originalName, final InputStream inputStream)
            throws IOException {
          return PREFIX + originalName;
        }
      });
    }
  }

  public static final class CssUrlRewriterWroManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      factory.addPreProcessor(new CssUrlRewritingProcessor());
      return factory;
    }
  }

  @Test
  public void testIncrementalChange() throws Exception {
    mojo = new Wro4jMojo() {
      @Override
      protected WroManagerFactory getManagerFactory() {
        return new WroManagerFactoryDecorator(super.getManagerFactory()) {
          @Override
          protected void onBeforeBuild(final Builder builder) {
            builder.setHashStrategy(mockHashStrategy);
          }
        };
      }
    };
    setUpMojo(mojo);
    final String hashValue = "SomeHashValue";
    when(mockHashStrategy.getHash(Mockito.any(InputStream.class))).thenReturn(hashValue);
    when(mockBuildContext.isIncremental()).thenReturn(true);
    when(mockBuildContext.getValue(Mockito.anyString())).thenReturn(hashValue);
    mojo.setIgnoreMissingResources(true);
    mojo.setBuildContext(mockBuildContext);
    //incremental build detects no change
    assertTrue(mojo.getTargetGroupsAsList().isEmpty());

    //incremental change detects change for all resources
    when(mockHashStrategy.getHash(Mockito.any(InputStream.class))).thenReturn("TotallyDifferentValue");
    assertFalse(mojo.getTargetGroupsAsList().isEmpty());
  }

  @Test
  public void shouldDetectIncrementalChangeOfImportedCss() throws Exception {
    final String parentResource = "parent.css";
    final String importResource = "imported.css";

    final WroModel model = new WroModel();
    model.addGroup(new Group("g1").addResource(Resource.create(parentResource)));
    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent(""));
    final String parentContent = String.format("@import url(%s)", importResource);
    when(mockLocator.locate(Mockito.eq(parentResource))).thenAnswer(answerWithContent(parentContent));

    mojo = new Wro4jMojo() {
      @Override
      protected WroManagerFactory newWroManagerFactory()
          throws MojoExecutionException {
        final DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
        managerFactory.setUriLocatorFactory(mockLocatorFactory);
        managerFactory.setModelFactory(WroTestUtils.simpleModelFactory(model));
        return managerFactory;
      }
    };
    final HashStrategy hashStrategy = mojo.getManagerFactory().create().getHashStrategy();
    setUpMojo(mojo);

    final String importedInitialContent = "initial";

    when(mockLocator.locate(Mockito.eq(importResource))).thenAnswer(answerWithContent(importedInitialContent));
    when(mockBuildContext.isIncremental()).thenReturn(true);
    when(mockBuildContext.getValue(Mockito.eq(parentResource))).thenReturn(
        hashStrategy.getHash(new ByteArrayInputStream(parentContent.getBytes())));
    when(mockBuildContext.getValue(Mockito.eq(importResource))).thenReturn(
        hashStrategy.getHash(new ByteArrayInputStream(importedInitialContent.getBytes())));
    mojo.setIgnoreMissingResources(true);
    mojo.setBuildContext(mockBuildContext);

    //incremental build detects no change
    assertTrue(mojo.getTargetGroupsAsList().isEmpty());

    when(mockLocator.locate(Mockito.eq(importResource))).thenAnswer(answerWithContent("Changed"));
    assertFalse(mojo.getTargetGroupsAsList().isEmpty());
  }

  private Answer<InputStream> answerWithContent(final String content) {
    return new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        return new ByteArrayInputStream(content.getBytes());
      }
    };
  }

  @After
  public void tearDown()
    throws Exception {
    FileUtils.deleteDirectory(destinationFolder);
    FileUtils.deleteDirectory(cssDestinationFolder);
    FileUtils.deleteDirectory(jsDestinationFolder);
    FileUtils.deleteQuietly(extraConfigFile);
  }
}
