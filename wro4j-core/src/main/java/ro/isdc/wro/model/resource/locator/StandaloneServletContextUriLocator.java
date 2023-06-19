package ro.isdc.wro.model.resource.locator;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAware;
import ro.isdc.wro.util.WroUtil;


/**
 * An extension of {@link ServletContextUriLocator} which is used for standalone (or build-time) solution. This class
 * existed initially as an anonymous class, but was extracted since it is complex enough to exist on its own.
 *
 * @author Alex Objelean
 * @since 1.7.2
 */
public final class StandaloneServletContextUriLocator
    extends ServletContextUriLocator implements StandaloneContextAware {
  private StandaloneContext standaloneContext;
  private static final Logger LOG = LoggerFactory.getLogger(StandaloneServletContextUriLocator.class);

  public StandaloneServletContextUriLocator() {
  }

  /**
   * This implementation will try to locate the provided resource inside contextFolder configured by standaloneContext.
   * If a resource cannot be located, the next contextFolder from the list will be tried. The first successful result
   * will be returned.
   */
  @Override
  public InputStream locate(final String uri)
      throws IOException {
    validState(standaloneContext != null, "Locator was not initialized properly. StandaloneContext missing.");

    Exception lastException = null;
    final String[] contextFolders = standaloneContext.getContextFolders();
    for(final String contextFolder : contextFolders) {
      try {
        return locateStreamWithContextFolder(uri, contextFolder);
      } catch(final IOException e) {
        lastException = e;
        LOG.debug("Could not locate: {} using contextFolder: {}", uri, contextFolder);
      }
    }
    final String exceptionMessage = String.format("No valid resource '%s' found inside any of contextFolders: %s", uri,
        Arrays.toString(standaloneContext.getContextFolders()));
    throw new IOException(exceptionMessage, lastException);
  }

  /**
   * TODO this is duplicated code (from super) -> find a way to reuse it.
   */
  private InputStream locateStreamWithContextFolder(final String uri, final String contextFolder)
      throws IOException, FileNotFoundException {
    if (getWildcardStreamLocator().hasWildcard(uri)) {
      final String fullPath = WroUtil.getFullPath(uri);
      final String realPath = contextFolder + fullPath;
      return getWildcardStreamLocator().locateStream(uri, new File(realPath));
    }

    final String uriWithoutPrefix = uri.replaceFirst(PREFIX, EMPTY);
    final File file = new File(contextFolder, uriWithoutPrefix);
    LOG.debug("Opening file: " + file.getPath());
    return new FileInputStream(file);
  }

  public void initialize(final StandaloneContext standaloneContext) {
    notNull(standaloneContext);
    this.standaloneContext = standaloneContext;
  }
}