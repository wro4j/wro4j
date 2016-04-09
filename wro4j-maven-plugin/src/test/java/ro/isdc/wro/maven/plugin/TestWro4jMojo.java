/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.manager.standalone.ExtensionsStandaloneManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
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
import ro.isdc.wro.model.resource.support.naming.DefaultHashEncoderNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.FolderHashEncoderNamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;
import ro.isdc.wro.util.concurrent.TaskExecutor;


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
  private Wro4jMojo victim;

  @Before
  public void setUp()
      throws Exception {
    initMocks(this);
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
    victim = new Wro4jMojo();
    setUpMojo(victim);
  }

  /**
   * Perform basic initialization with valid values of the provided mojo.
   */
  private void setUpMojo(final Wro4jMojo mojo)
      throws Exception {
    mojo.setIgnoreMissingResources(false);
    mojo.setParallelProcessing(false);
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

    final MavenProject mockMavenProject = Mockito.mock(MavenProject.class);
    final Model mockMavenModel = Mockito.mock(Model.class);
    final Build mockBuild = Mockito.mock(Build.class);
    Mockito.when(mockMavenProject.getModel()).thenReturn(mockMavenModel);
    Mockito.when(mockMavenModel.getBuild()).thenReturn(mockBuild);
    Mockito.when(mockBuild.getDirectory()).thenReturn(FileUtils.getTempDirectoryPath());

    mojo.setMavenProject(mockMavenProject);
    mojo.setBuildContext(mockBuildContext);
  }

  private void setWroFile(final String classpathResourceName)
      throws URISyntaxException {
    final URL url = getClass().getClassLoader().getResource(classpathResourceName);
    final File wroFile = new File(url.toURI());
    victim.setWroFile(wroFile);
    victim.setContextFolder(wroFile.getParentFile().getPath());
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
    victim.setTargetGroups("g1");
    victim.setIgnoreMissingResources(true);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void shouldFailWhenInvalidResourcesAreUsed()
      throws Exception {
    victim.setIgnoreMissingResources(false);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testNoDestinationFolderSet()
      throws Exception {
    victim.setDestinationFolder(null);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testOnlyCssDestinationFolderSet()
      throws Exception {
    victim.setCssDestinationFolder(cssDestinationFolder);
    victim.setDestinationFolder(null);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testOnlyJsDestinationFolderSet()
      throws Exception {
    victim.setJsDestinationFolder(jsDestinationFolder);
    victim.setDestinationFolder(null);
    victim.execute();
  }

  @Test
  public void testJsAndCssDestinationFolderSet()
      throws Exception {
    victim.setIgnoreMissingResources(true);
    victim.setJsDestinationFolder(jsDestinationFolder);
    victim.setCssDestinationFolder(cssDestinationFolder);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void cannotExecuteWhenInvalidResourcesPresentAndDoNotIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    victim.setIgnoreMissingResources(false);
    victim.execute();
  }

  @Test
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue()
      throws Exception {
    setWroWithInvalidResources();
    victim.setIgnoreMissingResources(true);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithWroManagerFactorySet()
      throws Exception {
    victim.setWroManagerFactory(ExceptionThrowingWroManagerFactory.class.getName());
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testInvalidMojoWithWroManagerFactorySet()
      throws Exception {
    victim.setWroManagerFactory("INVALID_CLASS_NAME");
    victim.execute();
  }

  @Test
  public void executeWithNullTargetGroupsProperty()
      throws Exception {
    victim.setIgnoreMissingResources(true);
    victim.setTargetGroups(null);
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithCustomManagerFactoryWithInvalidResourceAndNotIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    victim.setIgnoreMissingResources(false);
    victim.setWroManagerFactory(CustomManagerFactory.class.getName());
    victim.execute();
  }

  @Test
  public void testMojoWithCustomManagerFactoryWithInvalidResourceAndIgnoreMissingResources()
      throws Exception {
    setWroWithInvalidResources();
    victim.setIgnoreMissingResources(true);
    victim.setWroManagerFactory(CustomManagerFactory.class.getName());
    victim.execute();
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithConfigurableWroManagerFactory()
      throws Exception {
    setWroWithValidResources();
    victim.setIgnoreMissingResources(true);
    // by default a valid file is used, set null explicitly
    victim.setExtraConfigFile(null);
    victim.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    victim.execute();
  }

  @Test
  public void testMojoWithConfigurableWroManagerFactoryWithValidAndEmptyConfigFileSet()
      throws Exception {
    setWroWithValidResources();
    victim.setIgnoreMissingResources(true);
    victim.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    victim.execute();
  }

  @Test
  public void testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet()
      throws Exception {
    setWroWithValidResources();
    final String preProcessors = ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS + "=cssMin";
    FileUtils.write(extraConfigFile, preProcessors);

    victim.setIgnoreMissingResources(true);
    victim.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    victim.execute();
  }

  /**
   * Ignoring this test, since it is not reliable.
   */
  @Ignore
  @Test
  public void shouldBeFasterWhenRunningProcessingInParallel()
      throws Exception {
    //warmup
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
    //start actual test
    final long begin = System.currentTimeMillis();
    victim.setParallelProcessing(false);
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
    final long endSerial = System.currentTimeMillis();
    victim.setParallelProcessing(true);
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
    final long endParallel = System.currentTimeMillis();

    final long serial = endSerial - begin;
    final long parallel = endParallel - endSerial;
    LOG.info("serial took: {}ms", serial);
    LOG.info("parallel took: {}ms", parallel);
    assertTrue(String.format("Serial (%s) > Parallel (%s)", serial, parallel), serial > parallel);
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
    victim.setTaskExecutor(taskExecutor);
    victim.setIgnoreMissingResources(true);

    victim.setParallelProcessing(false);
    victim.execute();
    assertFalse(invoked.get());

    victim.setParallelProcessing(true);
    victim.execute();
    assertTrue(invoked.get());
  }

  @Test
  public void shouldComputedAggregatedFolderWhenContextPathIsSet()
      throws Exception {
    setWroWithValidResources();
    victim.setWroManagerFactory(CssUrlRewriterWroManagerFactory.class.getName());
    victim.setIgnoreMissingResources(true);
    final File cssDestinationFolder = new File(this.destinationFolder, "subfolder");
    cssDestinationFolder.mkdir();
    victim.setCssDestinationFolder(cssDestinationFolder);
    victim.execute();
    assertEquals("/subfolder", WroUtil.normalize(Context.get().getAggregatedFolderPath()));

    victim.setContextPath("app");
    victim.execute();
    assertEquals("/app", Context.get().getRequest().getContextPath());

    victim.setContextPath("/app/");
    victim.execute();
    assertEquals("/app", Context.get().getRequest().getContextPath());

    victim.setContextPath("/");
    victim.execute();
    assertEquals("/", Context.get().getRequest().getContextPath());
  }

  @Test(expected = MojoExecutionException.class)
  public void testMojoWithConfigurableWroManagerFactoryWithInvalidPreProcessor()
      throws Exception {
    setWroWithValidResources();
    final String preProcessors = ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS + "=INVALID";
    FileUtils.write(extraConfigFile, preProcessors);

    victim.setIgnoreMissingResources(true);
    victim.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    victim.execute();
  }

  @Test
  public void shouldGenerateGroupMappingUsingNoOpNamingStrategy()
      throws Exception {
    setWroWithValidResources();

    final File groupNameMappingFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    victim.setGroupNameMappingFile(groupNameMappingFile);
    victim.setIgnoreMissingResources(true);
    victim.execute();

    // verify
    final Properties groupNames = new Properties();
    groupNames.load(new FileInputStream(groupNameMappingFile));
    LOG.debug("groupNames: {}", groupNames);
    assertEquals("g1.js", groupNames.get("g1.js"));

    FileUtils.deleteQuietly(groupNameMappingFile);
  }

  @Test
  public void shouldGenerateGroupMappingUsingCustomNamingStrategy()
      throws Exception {
    setWroWithValidResources();

    final File groupNameMappingFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    victim.setWroManagerFactory(CustomNamingStrategyWroManagerFactory.class.getName());
    victim.setGroupNameMappingFile(groupNameMappingFile);
    victim.setIgnoreMissingResources(true);
    victim.execute();

    // verify
    final Properties groupNames = new Properties();
    groupNames.load(new FileInputStream(groupNameMappingFile));
    LOG.debug("groupNames: {}", groupNames);
    Assert.assertEquals(CustomNamingStrategyWroManagerFactory.PREFIX + "g1.js", groupNames.get("g1.js"));

    FileUtils.deleteQuietly(groupNameMappingFile);
  }

  /**
   * Uses a not existing folder to store groupNameMappingFile and proves that it is getting created instead of failing.
   */
  @Test
  public void shouldCreateMissingFolderForGroupNameMappingFile()
      throws Exception {
    final File parentFolder = new File(FileUtils.getTempDirectory(), "wro4j-" + UUID.randomUUID());
    try {
      setWroWithValidResources();
      final File groupNameMappingFile = new File(parentFolder, "groupMapping-" + new Date().getTime());

      victim.setWroManagerFactory(CustomNamingStrategyWroManagerFactory.class.getName());
      victim.setGroupNameMappingFile(groupNameMappingFile);
      victim.setIgnoreMissingResources(true);
      victim.execute();
    } finally {
      FileUtils.deleteQuietly(parentFolder);
    }
  }

  @Test
  public void shouldSkipSecondProcessingWhenIncrementalBuildEnabled()
      throws Exception {
    victim.setBuildContext(null);
    victim.setIncrementalBuildEnabled(true);
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
  }

  @Test
  public void shouldUseConfiguredNamingStrategy()
      throws Exception {
    setWroWithValidResources();

    final File extraConfigFile = new File(FileUtils.getTempDirectory(), "groupMapping-" + new Date().getTime());

    final Properties props = new Properties();
    // TODO create a properties builder
    props.setProperty(ConfigurableNamingStrategy.KEY, FolderHashEncoderNamingStrategy.ALIAS);
    props.list(new PrintStream(extraConfigFile));

    victim.setWroManagerFactory(ConfigurableWroManagerFactory.class.getName());
    victim.setExtraConfigFile(extraConfigFile);
    victim.setIgnoreMissingResources(true);
    victim.execute();

    FileUtils.deleteQuietly(extraConfigFile);
  }

  public static final class ExceptionThrowingWroManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
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

  public static class CustomManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
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

  public static final class CssUrlRewriterWroManagerFactory
      extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      factory.addPreProcessor(new CssUrlRewritingProcessor());
      return factory;
    }
  }

  @Test
  public void shouldDetectIncrementalChange()
      throws Exception {
    victim = new Wro4jMojo() {
      @Override
      protected WroManagerFactory newWroManagerFactory()
          throws MojoExecutionException {
        return new ExtensionsStandaloneManagerFactory().setHashStrategy(mockHashStrategy);
      }
    };
    setUpMojo(victim);
    final String hashValue = "SomeHashValue";
    when(mockHashStrategy.getHash(Mockito.any(InputStream.class))).thenReturn(hashValue);
    when(mockBuildContext.isIncremental()).thenReturn(true);
    when(mockBuildContext.getValue(Mockito.anyString())).thenReturn(hashValue);
    victim.setIgnoreMissingResources(true);
    // incremental build detects no change
    assertTrue(victim.getTargetGroupsAsList().isEmpty());

    // incremental change detects change for all resources
    when(mockHashStrategy.getHash(Mockito.any(InputStream.class))).thenReturn("TotallyDifferentValue");
    assertFalse(victim.getTargetGroupsAsList().isEmpty());
  }

  @Test
  public void shouldDetectIncrementalChangeOfImportedCss()
      throws Exception {
    final String importResource = "imported.css";

    configureMojoForModelWithImportedCssResource(importResource);
    // incremental build detects no change
    assertTrue(victim.getTargetGroupsAsList().isEmpty());

    when(mockLocator.locate(Mockito.eq(importResource))).thenAnswer(answerWithContent("Changed"));
    assertFalse(victim.getTargetGroupsAsList().isEmpty());
  }

  private void configureMojoForModelWithImportedCssResource(final String importResource)
      throws Exception {
    final String parentResource = "parent.css";

    final WroModel model = new WroModel();
    model.addGroup(new Group("g1").addResource(Resource.create(parentResource)));
    when(mockLocator.locate(Mockito.anyString())).thenAnswer(answerWithContent(""));
    final String parentContent = String.format("@import url(%s)", importResource);
    when(mockLocator.locate(Mockito.eq(parentResource))).thenAnswer(answerWithContent(parentContent));

    victim = new Wro4jMojo() {
      @Override
      protected WroManagerFactory newWroManagerFactory()
          throws MojoExecutionException {
        final DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
        managerFactory.setUriLocatorFactory(mockLocatorFactory);
        managerFactory.setModelFactory(WroTestUtils.simpleModelFactory(model));
        return managerFactory;
      }
    };
    final HashStrategy hashStrategy = victim.getManagerFactory().create().getHashStrategy();
    setUpMojo(victim);

    final String importedInitialContent = "initial";

    when(mockLocator.locate(Mockito.eq(importResource))).thenAnswer(answerWithContent(importedInitialContent));
    when(mockBuildContext.isIncremental()).thenReturn(true);
    when(mockBuildContext.getValue(Mockito.eq(parentResource))).thenReturn(
        hashStrategy.getHash(new ByteArrayInputStream(parentContent.getBytes())));
    when(mockBuildContext.getValue(Mockito.eq(importResource))).thenReturn(
        hashStrategy.getHash(new ByteArrayInputStream(importedInitialContent.getBytes())));
    victim.setIgnoreMissingResources(true);
  }

  @Test
  public void shouldIgnoreChangesOfGroupsWhichAreNotPartOfTargetGroups()
      throws Exception {
    final String importResource = "imported.css";

    configureMojoForModelWithImportedCssResource(importResource);
    victim.setTargetGroups("g2");

    // incremental build detects no change
    assertTrue(victim.getTargetGroupsAsList().isEmpty());

    when(mockLocator.locate(Mockito.eq(importResource))).thenAnswer(answerWithContent("Changed"));
    assertTrue(victim.getTargetGroupsAsList().isEmpty());
  }

  @Test
  public void shouldReuseGroupNameMappingFileWithIncrementalBuild()
      throws Exception {
    final File groupNameMappingFile = WroUtil.createTempFile();

    final Resource g1Resource = spy(Resource.create("1.js"));
    try {
      final WroModel model = new WroModel();
      model.addGroup(new Group("g1").addResource(g1Resource));
      model.addGroup(new Group("g2").addResource(Resource.create("2.js")));
      victim = new Wro4jMojo() {
        @Override
        protected WroManagerFactory newWroManagerFactory()
            throws MojoExecutionException {
          final DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
          managerFactory.setUriLocatorFactory(WroTestUtils.createResourceMockingLocatorFactory());
          managerFactory.setModelFactory(WroTestUtils.simpleModelFactory(model));
          managerFactory.setNamingStrategy(new DefaultHashEncoderNamingStrategy());

          return managerFactory;
        }
      };
      setUpMojo(victim);

      victim.setGroupNameMappingFile(groupNameMappingFile);

      assertEquals(2, victim.getTargetGroupsAsList().size());
      victim.execute();

      // Now mark it as incremental
      when(mockBuildContext.isIncremental()).thenReturn(true);

      final Properties groupNames = new Properties();
      groupNames.load(new FileInputStream(groupNameMappingFile));
      assertEquals(4, groupNames.entrySet().size());

      // change the uri of the resource from group a (equivalent to changing its content).
      when(g1Resource.getUri()).thenReturn("1a.js");

      assertEquals(1, victim.getTargetGroupsAsList().size());
      victim.execute();

      groupNames.load(new FileInputStream(groupNameMappingFile));
      // The number of persisted groupNames should still be unchanged, even though only a single group has been changed
      // after incremental build.
      assertEquals(4, groupNames.entrySet().size());
    } finally {
      FileUtils.deleteQuietly(groupNameMappingFile);
    }
  }

  /**
   * Test for the following scenario: when incremental build is performed, only changed resources are processed. Given
   * that there are two target groups, and a resource from only one group is changed incremental build should process
   * only that one group. However, if the targetFolder does not exist, all target groups must be processed.
   */
  @Test
  public void shouldProcessTargetGroupsWhenDestinationFolderDoesNotExist()
      throws Exception {
    victim = new Wro4jMojo() {
      @Override
      protected WroManagerFactory newWroManagerFactory()
          throws MojoExecutionException {
        return new ExtensionsStandaloneManagerFactory().setHashStrategy(mockHashStrategy);
      }
    };
    final String constantHash = "hash";
    when(mockHashStrategy.getHash(Mockito.any(InputStream.class))).thenReturn(constantHash);
    setUpMojo(victim);
    victim.setIgnoreMissingResources(true);

    final int totalGroups = 10;

    assertEquals(totalGroups, victim.getTargetGroupsAsList().size());

    when(mockBuildContext.isIncremental()).thenReturn(true);
    when(mockBuildContext.getValue(Mockito.anyString())).thenReturn(constantHash);

    assertEquals(0, victim.getTargetGroupsAsList().size());

    // delete target folder
    destinationFolder.delete();

    assertEquals(totalGroups, victim.getTargetGroupsAsList().size());

    victim.doExecute();
  }

  private Answer<InputStream> answerWithContent(final String content) {
    return new Answer<InputStream>() {
      public InputStream answer(final InvocationOnMock invocation)
          throws Throwable {
        return new ByteArrayInputStream(content.getBytes());
      }
    };
  }

  /**
   * Verify that the plugin execution does not fail when one of the context folder is invalid.
   */
  @Test
  public void shouldUseMultipleContextFolders()
      throws Exception {
    final String defaultContextFolder = victim.getContextFoldersAsCSV();
    victim.setTargetGroups("contextRelative");

    victim.setContextFolder("invalid, " + defaultContextFolder);
    victim.doExecute();

    // reversed order should work the same
    victim.setContextFolder(defaultContextFolder + ", invalid");
    victim.doExecute();
  }

  @Test
  public void shouldSkipExecutionWhenSkipIsEnabled()
      throws Exception {
    victim.setSkip(false);
    try {
      victim.execute();
      fail("should have failed");
    } catch (final MojoExecutionException e) {
    }
    victim.setSkip(true);
    victim.execute();
  }

  @Test
  public void shouldRefreshParentFolderWhenBuildContextSet() throws Exception {
    final BuildContext buildContext = Mockito.mock(BuildContext.class);
    victim.setBuildContext(buildContext);
    testMojoWithConfigurableWroManagerFactoryWithValidConfigFileSet();
    verify(buildContext, Mockito.atLeastOnce()).refresh(Mockito.eq(destinationFolder));
  }

  @After
  public void tearDown()
      throws Exception {
    victim.clean();
    FileUtils.deleteDirectory(destinationFolder);
    FileUtils.deleteDirectory(cssDestinationFolder);
    FileUtils.deleteDirectory(jsDestinationFolder);
    FileUtils.deleteQuietly(extraConfigFile);
  }
}
