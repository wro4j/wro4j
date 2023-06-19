package ro.isdc.wro.maven.plugin.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import ro.isdc.wro.manager.factory.standalone.StandaloneContext;


/**
 * Encapsulates the details about computation of aggregated folderPath in maven plugin.
 *
 * @author Alex Objelean
 * @since 1.7.2
 */
public class AggregatedFolderPathResolver {
  private File cssDestinationFolder;
  private File buildDirectory;
  private File buildFinalName;
  private File destinationFolder;
  private String contextFoldersAsCSV;
  private Log log;

  /**
   * The idea is to compute the aggregatedFolderPath based on a root folder. The root folder is determined by comparing
   * the cssTargetFolder (the folder where aggregated css files are located) with build directory or contextFolder. If
   * rootFolder is null, then the result is also null (equivalent to using the cssTargetFolder the same as the root
   * folder.
   *
   * @return the aggregated folder path, based on the cssDestinationFolder (if set) and the build folder or the
   *         contextFolder.
   */
  public String resolve() {
    notNull(buildDirectory, "Build directory cannot be null!");
    notNull(log, "Logger cannot be null!");
    String result = null;
    final File cssTargetFolder = cssDestinationFolder == null ? destinationFolder : cssDestinationFolder;
    File rootFolder = null;
    notNull(cssTargetFolder, "cssTargetFolder cannot be null!");

    if (buildFinalName != null && cssTargetFolder.getPath().startsWith(buildFinalName.getPath())) {
      rootFolder = buildFinalName;
    } else if (cssTargetFolder.getPath().startsWith(buildDirectory.getPath())) {
      rootFolder = buildDirectory;
    } else {
      // find first best match
      for (final String contextFolder : getContextFolders()) {
        if (cssTargetFolder.getPath().startsWith(contextFolder)) {
          rootFolder = new File(contextFolder);
          break;
        }
      }
    }
    log.debug("buildDirectory: " + buildDirectory);
    log.debug("contextFolders: " + contextFoldersAsCSV);
    log.debug("cssTargetFolder: " + cssTargetFolder);
    log.debug("rootFolder: " + rootFolder);
    if (rootFolder != null) {
      result = StringUtils.removeStart(cssTargetFolder.getPath(), rootFolder.getPath());
    }
    log.debug("computedAggregatedFolderPath: " + result);
    return result;
  }

  /**
   * Use {@link StandaloneContext} to tokenize the contextFoldersAsCSV value.
   */
  private String[] getContextFolders() {
    final StandaloneContext context = new StandaloneContext();
    context.setContextFoldersAsCSV(contextFoldersAsCSV);
    return context.getContextFolders();
  }

  public AggregatedFolderPathResolver setCssDestinationFolder(final File cssDestinationFolder) {
    this.cssDestinationFolder = cssDestinationFolder;
    return this;
  }

  public AggregatedFolderPathResolver setBuildDirectory(final File buildDirectory) {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public AggregatedFolderPathResolver setBuildFinalName(final File buildFinalName) {
    this.buildFinalName = buildFinalName;
    return this;
  }

  public AggregatedFolderPathResolver setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
    return this;
  }

  public AggregatedFolderPathResolver setContextFoldersAsCSV(final String contextFoldersAsCSV) {
    this.contextFoldersAsCSV = contextFoldersAsCSV;
    return this;
  }

  public AggregatedFolderPathResolver setLog(final Log log) {
    this.log = log;
    return this;
  }
}
