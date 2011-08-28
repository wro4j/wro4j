package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
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
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class JarWildcardStreamLocator extends DefaultWildcardStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(JarWildcardStreamLocator.class);

  public JarWildcardStreamLocator() {
  }

  /**
   * Finds the specified URI pattern inside a JAR file. If the specified file isn't a valid JAR default strategy will be
   * used instead.
   */
  @Override
  public InputStream locateStream(final String uri, final File folder)
    throws IOException {
    final File jarPath = new File(StringUtils.substringAfter(StringUtils.substringBeforeLast(folder.getPath(), "!"), "file:"));
    LOG.debug("jarPath: {}", jarPath);
    for (final String supportedExtension : getSupportedContainerExtensions()) {
      LOG.debug("\tsupportedExtension: {}", supportedExtension);
      if (jarPath.getPath().endsWith(supportedExtension)) {
        LOG.debug("\t\tLocating stream from jar");
        return locateStreamFromJar(uri, jarPath);
      }
    }
    return super.locateStream(uri, folder);
  }


  /**
   * Returns a list of file extensions of all valid JAR files.
   *
   * @return A {@link List} of file extensions including the final dot. Valid examples are: .jar, .war. By default it
   *         only supports .jar extension.
   */
  protected List<String> getSupportedContainerExtensions() {
    return Arrays.asList(".jar");
  }


  /**
   * Validates an entry against a wildcard and determines whether the pattern matches or not. If the entry is accepted
   * this will be included in the result {@link InputStream}.
   *
   * @param entry Entry to evaluate. It cannot be null.
   * @param wildcard Wildcard to match. It cannot be null or empty.
   *
   * @return <code>true</code> if the expression matches, <code>false</code> otherwise.
   */
  protected boolean accept(final JarEntry entry, final String wildcard) {
    return FilenameUtils.wildcardMatch(entry.getName(), wildcard);
  }


  /**
   * Opens the specified JAR file and returns a valid handle.
   *
   * @param jarFile Location of the valid JAR file to read. It cannot be null.
   *
   * @return A valid {@link JarFile} to read resources.
   * @throws IllegalArgumentException If the file cannot be opened because an {@link IOException}.
   */
  protected JarFile open(final File jarFile) {
    try {
      Validate.isTrue(jarFile.exists(), "The JAR file must exists.");

      return new JarFile(jarFile);
    } catch (final IOException ex) {
      throw new IllegalArgumentException("Cannot read the JAR file: " + jarFile, ex);
    }
  }


  /**
   * Finds the specified wildcard-URI resource(s) inside a JAR file and returns an {@link InputStream} to read a bundle
   * of matching resources.
   *
   * @param uri Resource(s) URI to match. It cannot be null or empty.
   * @param jarPath A valid JAR file. It cannot be null.
   *
   * @return A valid {@link InputStream} to read the bundle. Clients are responsible of closing this {@link InputStream}
   *         .
   *
   * @throws IOException If there's any error reading the JAR file.
   */
  protected final InputStream locateStreamFromJar(final String uri, final File jarPath)
    throws IOException {
    String classPath = FilenameUtils.getPath(uri);
    final String wildcard = FilenameUtils.getName(uri);

    if (classPath.startsWith(ClasspathUriLocator.PREFIX)) {
      classPath = StringUtils.substringAfter(classPath, ClasspathUriLocator.PREFIX);
    }

    final JarFile file = open(jarPath);

    final Enumeration<JarEntry> entries = file.entries();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();

      if (entry.getName().startsWith(classPath) && accept(entry, wildcard)) {
        final InputStream is = file.getInputStream(entry);
        IOUtils.copy(is, out);
        is.close();
      }
    }

    return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
  }
}
