package ro.isdc.wro.maven.plugin.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;


/**
 * Encapsulate the details about state persisted across multiple build runs. This class hides the details about the
 * storage used to persist the data produced during a build. By default the {@link BuildContext} is used, but if it is
 * not available - the alternate storage (a properties file stored in the file system) is used.
 *
 * @author Alex Objelean
 * @created 1 Sep 2013
 * @since 1.7.1
 */
public class BuildContextHolder {
  private static final Logger LOG = LoggerFactory.getLogger(BuildContextHolder.class);
  private static final String ROOT_FOLDER_NAME = ".wro4j";
  private static final String FALLBACK_STORAGE_FILE_NAME = "buildContext.properties";
  /**
   * Responsible for build storage persistence. Uses configured {@link BuildContext} as a primary storage object.
   */
  private final BuildContext buildContext;
  private final File buildDirectory;
  private Properties fallbackStorage;
  private boolean incrementalBuildEnabled;

  /**
   * @return an instance of {@link BuildContextHolder} which uses temp folder as root location of persisted data.
   */
  BuildContextHolder() {
    this(null, null);
  }

  /**
   * @param buildContext
   *          - The context provided by maven. If this is not provided, the default mechanism for persisting build data
   *          is used.
   * @param buildDirectory
   *          the folder where the build output is created. If this value is null, the temp folder will be used.
   */
  public BuildContextHolder(final BuildContext buildContext, final File buildDirectory) {
    this.buildContext = buildContext;
    this.buildDirectory = buildDirectory == null ? FileUtils.getTempDirectory() : buildDirectory;

    try {
      initFallbackStorage();
    } catch (final IOException e) {
      LOG.warn("Cannot use fallback storage. No build context storage will be used.", e);
    }
  }

  private void initFallbackStorage()
      throws IOException {
    final File fallbackStorageFile = getFallbackStorageFile();
    if (!fallbackStorageFile.exists()) {
      fallbackStorageFile.getParentFile().mkdirs();
      fallbackStorageFile.createNewFile();
    }
    fallbackStorage = new Properties();
    fallbackStorage.load(new FileInputStream(fallbackStorageFile));
    LOG.debug("loaded fallback storage: {}", fallbackStorage);
  }

  /**
   * @return the File where the fallback storage is persisted.
   */
  protected File getFallbackStorageFile() {
    final File rootFolder = new File(buildDirectory, ROOT_FOLDER_NAME);
    return new File(rootFolder, FALLBACK_STORAGE_FILE_NAME);
  }

  /**
   * @param key
   *          of the value to retrieve.
   * @return the persisted value stored under the provided key. If no value exist, the returned result will be null.
   */
  public String getValue(final String key) {
    String value = null;
    if (buildContext != null) {
      value = (String) buildContext.getValue(key);
    }
    if (value == null) {
      value = fallbackStorage.getProperty(key);
    }
    return value;
  }

  /**
   * @param key
   *          to associate with the value to be persisted.
   * @param value
   *          to persist.
   */
  public void setValue(final String key, final String value) {
    LOG.debug("storing key: '{}' and value: '{}'", key, value);
    if (buildContext != null) {
      buildContext.setValue(key, value);
    }
    // always use fallback
    if (value != null) {
      fallbackStorage.setProperty(key, value);
    } else {
      fallbackStorage.remove(key);
    }
    try {
      // immediately persist
      fallbackStorage.store(new FileOutputStream(getFallbackStorageFile()), "Generated");
      LOG.debug("fallback storage updated");
    } catch (final IOException e) {
      LOG.warn("Cannot store value: {}, because {}.", value, e.getMessage());
    }
  }

  public void setIncrementalBuildEnabled(final boolean incrementalBuildEnabled) {
    this.incrementalBuildEnabled = incrementalBuildEnabled;
  }

  /**
   * @return the flag indicating the incremental build change. A build is incremental, when the modified resources
   *         should be processed only. Useful to avoid unnecessary processing when there is actually no change detected.
   */
  public boolean isIncrementalBuild() {
    return buildContext != null ? buildContext.isIncremental() || incrementalBuildEnabled : incrementalBuildEnabled;
  }

  /**
   * Destroy the persisted storage and all stored data.
   */
  public void destroy() {
    fallbackStorage.clear();
    FileUtils.deleteQuietly(getFallbackStorageFile());
  }
}
