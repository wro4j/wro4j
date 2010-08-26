/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link WildcardStreamLocator}.
 *
 * @author Alex Objelean
 * @created May 8, 2010
 */
public class DefaultWildcardStreamLocator
    implements WildcardStreamLocator {
  /**
   * Logger.
   */
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
  private static final String RECURSIVE_WILDCARD = "**";
  /**
   * Comparator used to sort files in alphabetical ascending order.
   */
  public static final Comparator<File> ASCENDING_ORDER = new Comparator<File>() {
    public int compare(final File o1, final File o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  /**
   * Comparator used to sort files in alphabetical descending order.
   */
  public static final Comparator<File> DESCENDING_ORDER = new Comparator<File>() {
    public int compare(final File o1, final File o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  /**
   * {@inheritDoc}
   */
  public boolean hasWildcard(final String uri) {
    return uri.matches(WILDCARD_REGEX);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public InputStream locateStream(final String uri, final File folder)
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

    final String uriFolder = FilenameUtils.getFullPath(uri);

    final WildcardFileFilter fileFilter = new WildcardFileFilter(wildcard);
    final IOFileFilter folderFilter = new IOFileFilterDecorator(getFolderFilter(wildcard)) {
      @Override
      public boolean accept(final File dir, final String name) {
        final boolean accept = super.accept(dir, name);
        LOG.debug("accept: " + dir.getPath() + " | name : " + name);
        return accept;
      }
    };
    final Collection<File> files = FileUtils.listFiles(folder, fileFilter, folderFilter);
    //TODO remove duplicates if needed:
    //if (config.removeDuplicates) {
    //}

//    sortFiles(files);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (files.isEmpty()) {
      final String message = "No files found inside the " + folder.getPath() + " for wildcard: " + wildcard;
      LOG.warn(message);
    }
    for (final File file : files) {
      LOG.debug("file: " + file.getName());
      final InputStream is = new FileInputStream(file);
      IOUtils.copy(is, out);
    }
    out.close();
    return new ByteArrayInputStream(out.toByteArray());
  }
//
//  /**
//   * Sort the files collection using by default alphabetical order. Override this method to provide a different type of
//   * sorting. Or do nothing to leave it with its natural order.
//   *
//   * @param files
//   *          - the collection to sort.
//   */
//  protected void sortFiles(final Collection<File> files) {
//    Collections.sort(new ArrayList<File>(files), ASCENDING_ORDER);
//  }

  /**
   * @param wildcard
   *          to use to determine if the folder filter should be recursive or not.
   * @return filter to be used for folders.
   */
  private IOFileFilter getFolderFilter(final String wildcard) {
    final boolean recursive = wildcard.contains(RECURSIVE_WILDCARD);
    return recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE;
  }
}
