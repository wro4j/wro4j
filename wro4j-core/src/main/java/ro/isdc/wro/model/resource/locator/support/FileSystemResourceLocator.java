/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.util.StringUtils;


/**
 * {@link ResourceLocator} capable to work with files.
 *
 * @author Alex Objelean
 * @created 2 apr 2011
 */
public class FileSystemResourceLocator extends AbstractResourceLocator {
  private final File file;
  public FileSystemResourceLocator(final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File cannot be null!");
    }
    this.file = file;
  }
  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
    throws IOException {
    return new FileInputStream(this.file);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long lastModified() {
    return this.file.lastModified();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator createRelative(final String relativePath)
    throws IOException {
    final String folder = FilenameUtils.getFullPath(file.getPath());
    // remove '../' & normalize the path.
    final String pathToUse = StringUtils.normalizePath(folder + relativePath);
    return new FileSystemResourceLocator(new File(pathToUse));
  }
}
