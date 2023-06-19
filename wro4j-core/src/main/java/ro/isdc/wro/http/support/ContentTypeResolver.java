package ro.isdc.wro.http.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.WroUtil;


/**
 * Responseible for resolving a content based on file name.
 *
 * @author Alex Objelean
 */
public class ContentTypeResolver {
  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  private static final Logger LOG = LoggerFactory.getLogger(ContentTypeResolver.class);
  private final static Map<String, String> defaultContentTypeMap = new HashMap<String, String>();
  private final static Set<String> requiresCharset = new HashSet<String>();

  static {
    defaultContentTypeMap.put("txt", "text/plain");
    defaultContentTypeMap.put("css", "text/css");
    defaultContentTypeMap.put("html", "text/html");
    defaultContentTypeMap.put("htm", "text/html");
    defaultContentTypeMap.put("xml", "application/xml");
    defaultContentTypeMap.put("xhtml", "application/xhtml+xml");
    defaultContentTypeMap.put("js", "application/javascript");
    defaultContentTypeMap.put("png", "image/png");
    defaultContentTypeMap.put("gif", "image/gif");
    defaultContentTypeMap.put("jpg", "image/jpeg");
    defaultContentTypeMap.put("jpeg", "image/jpeg");
    // font types
    defaultContentTypeMap.put("eot", "application/vnd.ms-fontobject");
    defaultContentTypeMap.put("otf", "application/x-font-opentype");
    defaultContentTypeMap.put("svg", "image/svg+xml");
    defaultContentTypeMap.put("woff", "application/font-woff");
    defaultContentTypeMap.put("woff2", "application/font-woff2");

    requiresCharset.add("text/css");
    requiresCharset.add("text/html");
    requiresCharset.add("text/plain");
    requiresCharset.add("application/xml");
    requiresCharset.add("application/xhtml+xml");
    requiresCharset.add("application/javascript");
  }

  /**
   * <p>Returns a valid HTTP contentType's for a given filename. It first relies on the custom defaultContentTypeMap and if
   * not found it will fall back to defaultFileTypeMap from javax.activation.FileTypeMap. Examples: - "somefile.css"
   * resolves to "text/css". - "somefile.js.png" resolves to "image/png" - "/blah/index.html resolves to "text/html".</p>
   *
   * <p>The implementation uses reflection to load <code>javax.activation.FileTypeMap</code> class (available in jdk6) in
   * order to be compatible with jdk5. If this class is not available, the default content type is returned.</p>
   *
   * @param fileName
   *          with an filename extension
   * @return contentType
   */
  public static String get(final String fileName) {
    final String extension = FilenameUtils.getExtension(WroUtil.removeQueryString(fileName.toLowerCase()));
    if (defaultContentTypeMap.containsKey(extension)) {
      return defaultContentTypeMap.get(extension);
    }
    try {
      final Class<?> fileTypeMapClass = ClassLoader.getSystemClassLoader().loadClass("javax.activation.FileTypeMap");
      LOG.debug("using {} to resolve contentType", fileTypeMapClass.getName());
      final Object fileTypeMap = fileTypeMapClass.getMethod("getDefaultFileTypeMap").invoke(fileTypeMapClass);
      return (String) fileTypeMapClass.getMethod("getContentType", String.class).invoke(fileTypeMap, fileName);
    } catch (final Exception e) {
      LOG.debug("FileTypeMap is not available (probably jdk5 is used). Exception {}, with message: {}", e.getClass(),
          e.getMessage());
      LOG.debug("Will use default content type: {} for fileName: {}", DEFAULT_CONTENT_TYPE, fileName);
    }
    return DEFAULT_CONTENT_TYPE;
  }

  /**
   * Returns a valid HTTP contentType's for a given filename with charset. It first relies on the custom
   * defaultContentTypeMap and if not found it will fall back to defaultFileTypeMap from javax.activation.FileTypeMap.
   * Examples: - ("somefile.css", "UTF-8") resolves to "text/css"; charset=UTF-8". - ("somefile.js.png", "UTF-8")
   * resolves to "image/png" - ("/blah/index.html, "UTF-8") resolves to "text/html; charset=8"
   *
   * @param fileName
   *          with an filename extension
   * @param encoding
   *          which encoding to use
   * @return contentType
   */
  public static String get(final String fileName, final String encoding) {
    final String contentType = get(fileName);
    if (requiresCharset.contains(contentType)) {
      return contentType + "; charset=" + encoding;
    }
    return contentType;
  }
}