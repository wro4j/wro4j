/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.Function;


/**
 * Default implementation of {@link WildcardStreamLocator}.
 *
 * @author Alex Objelean
 * @created May 8, 2010
 */
public class DefaultWildcardStreamLocator
    implements WildcardStreamLocator, WildcardExpandedHandlerAware {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultWildcardStreamLocator.class);
  /**
   * Character to distinguish wildcard inside the uri.
   */
  public static final String RECURSIVE_WILDCARD = "**";
  /**
   * Character to distinguish wildcard inside the uri. If the file name contains '*' or '?' character, it is considered
   * a wildcard.
   * <p>
   * A string is considered to contain wildcard if it doesn't start with http(s) and contains at least one of the
   * following characters: [?*].
   */
  private static final String WILDCARD_REGEX = "^(?:(?!http))(.)*[\\*\\?]+(.)*";
  /**
   * Ensures File's natural ordering across different platforms.
   */
  private static final Comparator<File> ALPHABETIC_FILE_COMPARATOR = new Comparator<File>() {
    public int compare(final File o1, final File o2) {
      return o1.getPath().compareTo(o2.getPath());
    }
  };
  /**
   * Responsible for expanding wildcards, in other words for replacing one wildcard with a set of associated files.
   */
  private Function<Collection<File>, Void> wildcardExpanderHandler;

  /**
   * Creates a WildcardStream locator which doesn't care about detecting duplicate resources.
   */
  public DefaultWildcardStreamLocator() {
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasWildcard(final String uri) {
    return uri.matches(WILDCARD_REGEX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locateStream(final String uri, final File folder)
      throws IOException {
    final Collection<File> files = findMatchedFiles(new WildcardContext(uri, folder));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (final File file : files) {
      final InputStream is = new FileInputStream(file);
      IOUtils.copy(is, out);
      is.close();
    }
    return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
  }


  /**
   * Creates a {@link IOFileFilter} which collects found files into a collection and also populates a map with found
   * resources and corresponding files.
   *
   * @param wildcardContext
   * @param allFiles - where all found files and folders are collected.
   * @param uriToFileMap - mapping between found resources and corresponding files.
   */
  @SuppressWarnings("serial")
  private IOFileFilter createWildcardFileFilter(final WildcardContext wildcardContext, final Collection<File> allFiles,
    final Map<String, File> uriToFileMap) {
    notNull(wildcardContext);
    notNull(allFiles);
    notNull(uriToFileMap);
    return new WildcardFileFilter(wildcardContext.getWildcard()) {
      @Override
      public boolean accept(final File file) {
        final boolean accept = super.accept(file);
        if (accept) {
          allFiles.add(file);
          if (!file.isDirectory()) {
            final String relativeFilePath = file.getPath().replace(wildcardContext.getFolder().getPath(), "");
            final String uriFolder = FilenameUtils.getFullPathNoEndSeparator(wildcardContext.getUri());
            final String resourceUri = uriFolder + relativeFilePath.replace('\\', '/');
            uriToFileMap.put(resourceUri, file);
            LOG.debug("\tfoundUri: {}", resourceUri);
          }
        }
        return accept;
      }
    };
  }

  /**
   * @return a collection of files found inside a given folder for a search uri which contains a wildcard.
   */
  private Collection<File> findMatchedFiles(final WildcardContext wildcardContext)
      throws IOException {
    validate(wildcardContext);

    // maps resource uri's and corresponding file this map has to be ordered
    final Map<String, File> uriToFileMap = new TreeMap<String, File>();
    // Holds a set of all files (also folders, not only resources). This is useful for wildcard expander processing.
    final Set<File> allFilesAndFolders = new TreeSet<File>(ALPHABETIC_FILE_COMPARATOR);
    final IOFileFilter fileFilter = createWildcardFileFilter(wildcardContext, allFilesAndFolders, uriToFileMap);
    FileUtils.listFiles(wildcardContext.getFolder(), fileFilter, getFolderFilter(wildcardContext.getWildcard()));

    handleFoundResources(uriToFileMap, wildcardContext);
    triggerWildcardExpander(allFilesAndFolders);

    return uriToFileMap.values();
  }


  /**
   * Validates arguments used by {@link DefaultWildcardStreamLocator#findMatchedFiles(String, File)} method.
   *
   * @throws IOException if supplied arguments are invalid or cannot be handled by this locator.
   */
  private void validate(final WildcardContext wildcardContext)
    throws IOException {
    notNull(wildcardContext);
    final String uri = wildcardContext.getUri();
    final File folder = wildcardContext.getFolder();

    if (uri == null || folder == null || !folder.isDirectory()) {
      final StringBuffer message = new StringBuffer("Invalid folder provided");
      if (folder != null) {
        message.append(", with path: " + folder.getPath());
      }
      message.append(", with fileNameWithWildcard: " + uri);
      throw new IOException(message.toString());
    }
    if (!hasWildcard(uri)) {
      throw new IOException("No wildcard detected for the uri: " + uri);
    }
    LOG.debug("uri: {}", uri);
    LOG.debug("folder: {}", folder.getPath());
    LOG.debug("wildcard: {}", wildcardContext.getWildcard());

  }

  /**
   * Uses the wildcardExpanderHandler to process the found files, also directories.
   *
   * @param files
   *          a collection of found files after the wildcard has beed applied on the searched folder.
   */
  private final void triggerWildcardExpander(final Set<File> allFiles)
      throws IOException {
    if (wildcardExpanderHandler != null) {
      try {
        wildcardExpanderHandler.apply(allFiles);
      } catch (final Exception e) {
        // preserve exception type if the exception is already an IOException
        if (e instanceof IOException) {
          throw (IOException) e;
        }
        throw new IOException("Exception during expanding wildcard: " + e.getMessage());
      }
    }
  }


  /**
   * The default implementation does nothing. Useful for unit test to check if the order is as expected.
   *
   * @param uriToFileMap The map of resource uri's and corresponding files.
   * @param wildcardContext the context of the wildcard resources search
   * @VisibleForTestOnly
   */
  void handleFoundResources(final Map<String, File> uriToFileMap, final WildcardContext wildcardContext)
      throws IOException {
    LOG.debug("map files: {}", uriToFileMap.keySet());
    if (uriToFileMap.isEmpty()) {
      LOG.warn("No files found inside the {} for wildcard: {}", wildcardContext.getFolder().getPath(),
        wildcardContext.getWildcard());
    }
  }

  /**
   * @param wildcard
   *          to use to determine if the folder filter should be recursive or not.
   * @return filter to be used for folders.
   */
  private IOFileFilter getFolderFilter(final String wildcard) {
    final boolean recursive = wildcard.contains(RECURSIVE_WILDCARD);
    return recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public void setWildcardExpanderHandler(final Function<Collection<File>, Void> handler) {
    this.wildcardExpanderHandler = handler;
  }
}
