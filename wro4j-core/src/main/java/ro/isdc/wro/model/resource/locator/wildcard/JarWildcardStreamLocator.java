package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;


/**
 * Resolves classpath resources looking for wildcard patterns in both file system and in JAR files.
 * <p>
 * The {@link #locateStream(String, File)} overrides the default strategy defined in
 * {@link DefaultWildcardStreamLocator} and it tries to open the provided file as a JAR. If that's successfully opened
 * all entries inside this container will be verified against the wildcard pattern. If the JAR-lookup strategy fails,
 * default strategy is invoked.
 * </p>
 * <p>
 * For the moment this {@link WildcardStreamLocator} only supports a single wildcard.
 * </p>
 *
 * @author <a href="mailto:matias.mirabelli@globant.com">Matias Mirabelli</a>
 * @since 1.3.6
 */
public class JarWildcardStreamLocator
    extends DefaultWildcardStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(JarWildcardStreamLocator.class);
  /**
   * A {@link List} of file extensions including the final dot. Valid examples are: .jar, .war. By default it only
   * supports .jar extension.
   */
  private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".jar");
  private static final String EMBEDDED_JAR_PREFIX = "!" + File.separatorChar;
  private static final String PREFIX_FILE = "file:";

  /**
   * Finds the specified URI pattern inside a JAR file. If the specified file isn't a valid JAR default strategy will be
   * used instead.
   */
  @Override
  public InputStream locateStream(final String uri, final File folder)
      throws IOException {
	Validate.notNull(folder);
    final File jarPath = getJarFile(folder);
    if (isSupported(jarPath)) {
      return locateStreamFromJar(uri, jarPath);
    }
    return super.locateStream(uri, folder);
  }

  /**
   * @return true if the file is of a certain supported type.
   */
  private boolean isSupported(final File jarPath) {
    LOG.debug("jarPath: {}", jarPath);
    for (final String supportedExtension : SUPPORTED_EXTENSIONS) {
      if (jarPath.getPath().endsWith(supportedExtension) && !jarPath.getPath().contains(EMBEDDED_JAR_PREFIX)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @VisibleForTestOnly
   * @return the File corresponding to the folder from inside the jar.
   */
  File getJarFile(final File folder) {
    return new File(StringUtils.substringAfter(StringUtils.substringBeforeLast(folder.getPath(), "!"), PREFIX_FILE));
  }

  /**
   * Validates an entry against a wildcard and determines whether the pattern matches or not. If the entry is accepted
   * this will be included in the result {@link InputStream}.
   *
   * @param entry
   *          Entry to evaluate. It cannot be null.
   * @param wildcard
   *          Wildcard to match. It cannot be null or empty.
   * @return <code>true</code> if the expression matches, <code>false</code> otherwise.
   */
  private boolean accept(final String entryName, final String wildcard) {
    return FilenameUtils.wildcardMatch(entryName, wildcard);
  }

  /**
   * Opens the specified JAR file and returns a valid handle.
   *
   * @param jarFile
   *          Location of the valid JAR file to read. It cannot be null.
   * @return A valid {@link JarFile} to read resources.
   * @throws IllegalArgumentException
   *           If the file cannot be opened because an {@link IOException}.
   *  @VisibleForTestOnly
   */
  JarFile open(final File jarFile) throws IOException {
	Validate.isTrue(jarFile.exists(), "The JAR file must exists.");
    return new JarFile(jarFile);
  }

	/**
	 * Finds the specified wildcard-URI resource(s) inside a JAR file and returns an
	 * {@link InputStream} to read a bundle of matching resources.
	 *
	 * @param uri     Resource(s) URI to match. It cannot be null or empty.
	 * @param jarPath A valid JAR file. It cannot be null.
	 * @return A valid {@link InputStream} to read the bundle. Clients are
	 *         responsible of closing this {@link InputStream}
	 * @throws IOException If there's any error reading the JAR file.
	 */
	private InputStream locateStreamFromJar(final String uri, final File jarPath) throws IOException {

		LOG.debug("Locating stream from jar: {}", jarPath);
		final WildcardContext wildcardContext = new WildcardContext(uri, jarPath);
		String classPath = FilenameUtils.getPath(uri);

		if (classPath.startsWith(ClasspathUriLocator.PREFIX)) {
			classPath = StringUtils.substringAfter(classPath, ClasspathUriLocator.PREFIX);
		}

		try (JarFile jarFile = open(jarPath)) {

			final List<JarEntry> jarEntryList = Collections.list(jarFile.entries());
			final List<JarEntry> filteredJarEntryList = new ArrayList<>();
			final List<File> allFiles = new ArrayList<>();
			for (final JarEntry entry : jarEntryList) {
				final String entryName = entry.getName();
				// ignore the parent folder itself and accept only child resources
				final boolean isSupportedEntry = entryName.startsWith(classPath) && !entryName.equals(classPath)
						&& accept(entryName, wildcardContext.getWildcard());
				if (isSupportedEntry) {
					allFiles.add(new File(entryName));
					LOG.debug("\tfound jar entry: {}", entryName);
					filteredJarEntryList.add(entry);
				}
			}

			final ByteArrayOutputStream out = new ByteArrayOutputStream();

			triggerWildcardExpander(allFiles, wildcardContext);

			for (final JarEntry entry : filteredJarEntryList) {
				final InputStream is = jarFile.getInputStream(entry);
				IOUtils.copy(is, out);
				is.close();
			}

			return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
		}
	}

}
