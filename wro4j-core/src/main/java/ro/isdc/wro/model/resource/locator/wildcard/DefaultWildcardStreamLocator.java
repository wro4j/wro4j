/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.support.IOFileFilterDecorator;
import ro.isdc.wro.util.Transformer;


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
   * Character to distinguish wildcard inside the uri. If the file name contains '*' or '?' character, it is considered
   * a wildcard.
   * <p>
   * A string is considered to contain wildcard if it doesn't start with http(s) and contains at least one of the
   * following characters: [?*].
   */
  private static final String WILDCARD_REGEX = "^(?:(?!http))(.)*[\\*\\?]+(.)*";
  /**
   * Character to distinguish wildcard inside the uri.
   */
  public static final String RECURSIVE_WILDCARD = "**";
  private Transformer<Collection<File>> wildcardExpanderHandler;
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
    final Collection<File> files = findMatchedFiles(uri, folder);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (final File file : files) {
      final InputStream is = new FileInputStream(file);
      IOUtils.copy(is, out);
      is.close();
    }
    return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
  }

  /**
   * @return a collection of files found inside a given folder for a search uri which contains a wildcard.
   */
  private Collection<File> findMatchedFiles(final String uri, final File folder)
    throws IOException {
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

    final String wildcard = FilenameUtils.getName(uri);
    LOG.debug("uri: " + uri);
    LOG.debug("folder: " + folder.getPath());
    LOG.debug("wildcard: " + wildcard);

    //maps resource uri's and corresponding file
    //this map have to be ordered
    final Map<String, File> uriToFileMap = new TreeMap<String, File>();

    final String uriFolder = FilenameUtils.getFullPathNoEndSeparator(uri);
    final String parentFolderPath = folder.getPath();

    final IOFileFilter fileFilter = new IOFileFilterDecorator(new WildcardFileFilter(wildcard)) {
      @Override
      public boolean accept(final File file) {
        final boolean accept = super.accept(file);
        if (accept && !file.isDirectory()) {
          final String relativeFilePath = file.getPath().replace(parentFolderPath, "");
          final String resourceUri = uriFolder + relativeFilePath.replace('\\', '/');
          uriToFileMap.put(resourceUri, file);
          LOG.debug("foundUri: " + resourceUri);
        }
        return accept;
      }
    };
    FileUtils.listFiles(folder, fileFilter, getFolderFilter(wildcard));

    //TODO remove duplicates if needed:
    LOG.debug("map files: " + uriToFileMap.keySet());

    final Collection<File> files = uriToFileMap.values();
    if (files.isEmpty()) {
      final String message = "No files found inside the " + folder.getPath() + " for wildcard: " + wildcard;
      LOG.warn(message);
    }
    handleFoundFiles(files);
    return files;
  }


  /**
   * The default implementation uses the wildcardExpanderHandler to process the found files.
   *
   * @param files a collection of found files after the wildcard has beed applied on searched folder.
   */
  protected void handleFoundFiles(final Collection<File> files) throws IOException {
    LOG.debug("wildcardExpanderHandler: {}", wildcardExpanderHandler);
    if (wildcardExpanderHandler != null) {
      try {
        wildcardExpanderHandler.transform(files);
      } catch (final Exception e) {
        throw new IOException("Exception during expanding wildcard", e);
      }
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
  public void setWildcardExpanderHandler(final Transformer<Collection<File>> handler) {
    this.wildcardExpanderHandler = handler;
  }
}
