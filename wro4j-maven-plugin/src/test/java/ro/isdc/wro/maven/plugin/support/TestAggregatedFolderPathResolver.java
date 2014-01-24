package ro.isdc.wro.maven.plugin.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.util.WroUtil;

/**
 * @author Alex Objelean
 */
public class TestAggregatedFolderPathResolver {
  private static final String PATH_SEPARATOR = File.pathSeparator;
  private AggregatedFolderPathResolver victim;
  @Mock
  private Log log;
  private File buildDirectory;
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new AggregatedFolderPathResolver();
    WroUtil.createTempFile();
    buildDirectory = new File(FileUtils.getTempDirectory(), "wroTemp-" + new Date().getTime());
    victim.setBuildDirectory(buildDirectory);
    victim.setDestinationFolder(buildDirectory);
    victim.setLog(log);
  }

  @Test
  public void shouldResolveEmptyPathWhenDestinationFolderIsTheSameAsBuildDirectory() {
    assertEquals("", victim.resolve());
  }

  @Test
  public void shouldResolveToSubfolder() {
    final String subFolder = PATH_SEPARATOR + "css";
    victim.setDestinationFolder(new File(buildDirectory.getPath() + subFolder));
    assertEquals(subFolder, victim.resolve());
  }

  @Test
  public void shouldBeNullWhenDestinationFolderIsUnrelatedToBuildDirectory() {
    final File unrelatedFolder = new File(getClass().getResource("").getFile()).getParentFile();
    victim.setDestinationFolder(unrelatedFolder);
    assertEquals(null, victim.resolve());
  }

  @Test
  public void shouldUseFirstMatchWhenMultipleContextFoldersProvided() {
    final String baseSubfolder = PATH_SEPARATOR + "css";
    final String firstSubFolder = baseSubfolder + PATH_SEPARATOR + "first";
    final String firstCssDestinationPath = buildDirectory.getPath() + firstSubFolder;
    final String secondCssDestinationPath = buildDirectory.getPath() + baseSubfolder;
    victim.setCssDestinationFolder(new File(firstCssDestinationPath));
    victim.setContextFoldersAsCSV(firstCssDestinationPath + ", " + secondCssDestinationPath);
    assertEquals(firstSubFolder, victim.resolve());
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(buildDirectory);
  }
}
