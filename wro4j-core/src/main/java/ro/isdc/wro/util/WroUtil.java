/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.FastDateFormat;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Utility class.
 *
 * @author Alex Objelean
 */
public final class WroUtil {

  private static final String SEPARATOR_UNIX = "/";
  private static final String SEPARATOR_WINDOWS = "\\";
  /**
   * Empty line pattern.
   */
  public static final Pattern EMTPY_LINE_PATTERN = Pattern.compile(loadRegexpWithKey("emptyLine"), Pattern.MULTILINE);
  /**
   * Thread safe date format used to transform milliseconds into date as string to put in response header. The localy is
   * set explicitly to US to conform to specification.
   */
  private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("E, dd MMM yyyy HH:mm:ss z",
      TimeZone.getTimeZone("GMT"), Locale.US);
  /**
   * Patterns used to search for mangled Accept-Encoding header.
   */
  private static final Pattern PATTERN_ACCEPT_ENCODING = Pattern.compile(loadRegexpWithKey("requestHeader.acceptEncoding"));
  private static final Pattern PATTERN_GZIP = Pattern.compile(loadRegexpWithKey("requestHeader.gzip"));

  private static final AtomicInteger threadFactoryNumber = new AtomicInteger(1);
  public static final InputStream EMPTY_STREAM = new ByteArrayInputStream(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8));

  /**
   * Default private constructor to avoid instantiating this class.
   */
  private WroUtil() {
	  super();
  }

  /**
   * @return {@link ThreadFactory} with daemon threads.
   */
  public static ThreadFactory createDaemonThreadFactory(final String name) {
    return new ThreadFactory() {
      private final String prefix = "wro4j-" + name + "-" + threadFactoryNumber.getAndIncrement() + "-thread-";
      private final AtomicInteger threadNumber = new AtomicInteger(1);

      @Override
      public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable, prefix + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
      }
    };
  }

  /**
   * Transforms milliseconds into date format for response header of this form: Sat, 10 Apr 2010 17:31:31 GMT.
   *
   * @param milliseconds
   *          to transform
   * @return string representation of the date.
   */
  public static String toDateAsString(final long milliseconds) {
    return DATE_FORMAT.format(milliseconds);
  }

  /**
   * Retrieve pathInfo from a given location.
   *
   * @param request
   * @param location
   *          where to search contextPath.
   * @return pathInfo value.
   */
  public static String getPathInfoFromLocation(final HttpServletRequest request, final String location) {
    if (StringUtils.isEmpty(location)) {
      throw new IllegalArgumentException("Location cannot be empty string!");
    }
    final String contextPath = request.getContextPath();
    if (contextPath != null) {
      if (startsWithIgnoreCase(location, contextPath)) {
        return location.substring(contextPath.length());
      } else {
        return location;
      }
    }
    final String noSlash = location.substring(1);
    final int nextSlash = noSlash.indexOf('/');
    if (nextSlash == -1) {
      return "";
    }
    return noSlash.substring(nextSlash);
  }

  /**
   * <p>
   * Case insensitive check if a String starts with a specified prefix.
   * </p>
   * <p>
   * <code>null</code>s are handled without exceptions. Two <code>null</code> references are considered to be equal. The
   * comparison is case insensitive.
   * </p>
   *
   * <pre>
   * StringUtils.startsWithIgnoreCase(null, null)      = true
   * StringUtils.startsWithIgnoreCase(null, "abcdef")  = false
   * StringUtils.startsWithIgnoreCase("abc", null)     = false
   * StringUtils.startsWithIgnoreCase("abc", "abcdef") = true
   * StringUtils.startsWithIgnoreCase("abc", "ABCDEF") = true
   * </pre>
   *
   * @see java.lang.String#startsWith(String)
   * @param str
   *          the String to check, may be null
   * @param prefix
   *          the prefix to find, may be null
   * @return <code>true</code> if the String starts with the prefix, case insensitive, or both <code>null</code>
   * @since 2.4
   */
  public static boolean startsWithIgnoreCase(final String str, final String prefix) {
    return startsWith(str, prefix, true);
  }

  /**
   * Creates a folder like implementation for a class. Ex: com.mycompany.MyClass -> com/mycompany/
   *
   * @param clazz
   *          used as a base location for determining the package path.
   * @return a string representation of the path where the class resides.
   */
  public static String toPackageAsFolder(final Class<?> clazz) {
    Validate.notNull(clazz, "Class cannot be null!");
    return clazz.getPackage().getName().replace('.', '/');
  }

  /**
   * <p>
   * Check if a String starts with a specified prefix (optionally case insensitive).
   * </p>
   *
   * @see java.lang.String#startsWith(String)
   * @param str
   *          the String to check, may be null
   * @param prefix
   *          the prefix to find, may be null
   * @param ignoreCase
   *          indicates whether the compare should ignore case (case insensitive) or not.
   * @return <code>true</code> if the String starts with the prefix or both <code>null</code>
   */
  private static boolean startsWith(final String str, final String prefix, final boolean ignoreCase) {
    if (str == null || prefix == null) {
      return (str == null && prefix == null);
    }
    if (prefix.length() > str.length()) {
      return false;
    }
    return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
  }

  /**
   * Retrieve servletPath from a given location.
   *
   * @param location
   *          where to search the servletPath.
   * @return ServletPath string value.
   */
  public static String getServletPathFromLocation(final HttpServletRequest request, final String location) {
    return location.replace(getPathInfoFromLocation(request, location), StringUtils.EMPTY);
  }

  /**
   * Analyze headers of the request and searches for mangled (by proxy) for "Accept-Encoding" header and its mangled
   * variations and gzip header value and its mangled variations.
   *
   * @return true if this request support gzip encoding.
   */
  public static boolean isGzipSupported(final HttpServletRequest request) {
    if (request != null) {
      final Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames != null) {
        while (headerNames.hasMoreElements()) {
          final String headerName = headerNames.nextElement();
          final Matcher m = PATTERN_ACCEPT_ENCODING.matcher(headerName);
          if (m.find()) {
            final String headerValue = request.getHeader(headerName);
            final Matcher mValue = PATTERN_GZIP.matcher(headerValue);
            return mValue.find();
          }
        }
      }
    }
    return false;
  }

  /**
   * Transforms a java multi-line string into javascript multi-line string. This technique was found at
   * <a href="http://stackoverflow.com/questions/805107/multiline-strings-in-javascript/">http://stackoverflow.com/questions/805107/multiline-strings-in-javascript/</a>
   *
   * @param data
   *          a string containing new lines.
   * @return a string which being evaluated on the client-side will be treated as a correct multi-line string.
   */
  public static String toJSMultiLineString(final String data) {
    final StringBuilder result = new StringBuilder("[");
    if (data != null) {
      final String[] lines = data.split("\n");
      if (lines.length == 0) {
        result.append("\"\"");
      }
      for (int i = 0; i < lines.length; i++) {
        final String line = lines[i];
        result.append("\"");
        result.append(line.replace(SEPARATOR_WINDOWS, "\\\\").replace("\"", "\\\"").replaceAll("\\r|\\n", ""));
        // this is used to force a single line to have at least one new line (otherwise cssLint fails).
        if (lines.length == 1) {
          result.append("\\n");
        }
        result.append("\"");
        if (i < lines.length - 1) {
          result.append(",");
        }
      }
    }
    result.append("].join(\"\\n\")");
    return result.toString();
  }

  /**
   * Utility used to verify that requestURI matches provided path
   */
  public static boolean matchesUrl(final HttpServletRequest request, final String path) {
    final Pattern pattern = Pattern.compile(".*" + path + "[/]?", Pattern.CASE_INSENSITIVE);
    if (request.getRequestURI() != null) {
      final Matcher m = pattern.matcher(request.getRequestURI());
      return m.matches();
    }
    return false;
  }

  /**
   * A factory method for creating a {@link ResourcePostProcessor} based on provided {@link ResourcePreProcessor}.
   *
   * @param preProcessor
   *          {@link ResourcePreProcessor} to use.
   * @return instance of {@link ResourcePostProcessor}.
   */
  public static ResourcePostProcessor newResourceProcessor(final Resource resource,
      final ResourcePreProcessor preProcessor) {
    return new ResourcePostProcessor() {
      @Override
      public void process(final Reader reader, final Writer writer)
          throws IOException {
        preProcessor.process(resource, reader, writer);
      }
    };
  }

  /**
   * A simple way to create a {@link WroModelFactory}.
   *
   * @param model
   *          {@link WroModel} instance to be returned by the factory.
   */
  public static WroModelFactory factoryFor(final WroModel model) {
    return new WroModelFactory() {
      @Override
      public WroModel create() {
        return model;
      }

      @Override
      public void destroy() {
      }
    };
  }

  public static <T> ObjectFactory<T> simpleObjectFactory(final T object) {
    return new ObjectFactory<T>() {
      @Override
      public T create() {
        return object;
      }
    };
  }

  /**
   * Load the regular expression stored in in regexp.properties resource file.
   *
   * @param key
   *          the key of the regexp to load.
   * @return regular expression value.
   */
  public static String loadRegexpWithKey(final String key) {

	try (InputStream stream = WroUtil.class.getResourceAsStream("regexp.properties")) {
      final Properties props = new RegexpProperties().load(stream);
      return props.getProperty(key);
    } catch (final IOException e) {
      throw new WroRuntimeException("Could not load pattern with key: " + key + " from property file", e);
    }
  }

  /**
   * @return the implementation version of wro4j.
   */
  public static String getImplementationVersion() {
    return WroUtil.class.getPackage().getImplementationVersion();
  }

  /**
   * Copy and close the reader and writer streams.
   *
   * @param reader
   *          The source stream.
   * @param writer
   *          The destination stream.
   * @throws IOException
   *           If content cannot be copy.
   */
  public static void safeCopy(final Reader reader, final Writer writer)
      throws IOException {
    try (reader;writer) {
      IOUtils.copy(reader, writer);
    }
  }

  /**
   * @return a generated {@link File} with unique name located in temp folder.
   */
  public static File createTempFile() {
    return createTempFile("temp");
  }

  /**
   * Creates a temp file which has a certain extension.
   *
   * @param extension
   *          of the created temp file.
   */
  public static File createTempFile(final String extension) {
    try {
      final String fileName = String.format("wro4j-%s.%s", UUID.randomUUID().toString(), extension);
      final File file = new File(createTempDirectory(), fileName);
      file.createNewFile();
      return file;
    } catch (final IOException e) {
      throw WroRuntimeException.wrap(e);
    }
  }

  /**
   * @return a folder with unique name..
   */
  public static File createTempDirectory() {
    final String fileName = String.format("wro4j-%s", UUID.randomUUID().toString());
    final File file = new File(FileUtils.getTempDirectory(), fileName);
    file.mkdir();
    return file;
  }

  /**
   * Join two paths (using unix separator) and make sure to use exactly one separator (by adding or removing one if required).
   */
  public static String joinPath(final String left, final String right) {
    String leftHand = left;
    if (!left.endsWith(SEPARATOR_UNIX)) {
      leftHand += SEPARATOR_UNIX;
    }
    return leftHand + right.replaceFirst("^/(.*)", "$1");
  }

  /**
   * Cleans the image url by trimming result and removing \' or \" characters if such exists.
   *
   * @param imageUrl
   *          to clean.
   * @return cleaned image URL.
   */
  public static final String cleanImageUrl(final String imageUrl) {
    notNull(imageUrl);
    return imageUrl.replace('\'', ' ').replace('\"', ' ').trim();
  }

  /**
   * Removes the query string from the provided path (everything followed by '?' including the question mark).
   */
  public static final String removeQueryString(final String path) {
    return path.replaceFirst("\\?.*", StringUtils.EMPTY);
  }

  /**
   * @return current working directory
   */
  public static final File getWorkingDirectory() {
    return new File(System.getProperty("user.dir"));
  }

  /**
   * Similar to {@link FilenameUtils#getFullPath(String)}, but fixes the problem with Windows platform for situations
   * when the path starts with "/" (servlet context relative resources) which are resolved to "\" on windows.
   *
   * @param path
   *          to compute filePath from.
   * @return full path from the provided path.
   */
  public static final String getFullPath(final String path) {
    final String fullPath = FilenameUtils.getFullPath(path);
    return replaceWithServletContextSeparatorIfNedded(fullPath);
  }

  /**
   * Similar to {@link FilenameUtils#normalize(String)}, but fixes the problem with Windows platform for situations
   * when the path starts with "/" (servlet context relative resources) which are resolved to "\" on windows.
   *
   * @param path
   *          to compute filePath from.
   * @return full path from the provided path.
   */
  public static final String normalize(final String path) {
    final String normalized = FilenameUtils.normalize(path);
    return replaceWithServletContextSeparatorIfNedded(normalized);
  }

  private static String replaceWithServletContextSeparatorIfNedded(String normalized) {
    if (normalized.startsWith(SEPARATOR_WINDOWS) || normalized.contains(SEPARATOR_WINDOWS)) {
      normalized = normalized.replace(SEPARATOR_WINDOWS, ServletContextUriLocator.PREFIX);
    }
    return normalized;
  }
}
