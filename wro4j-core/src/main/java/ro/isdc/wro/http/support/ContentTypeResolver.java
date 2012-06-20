package ro.isdc.wro.http.support;

import org.apache.commons.io.FilenameUtils;

import javax.activation.FileTypeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentTypeResolver {

  private final static FileTypeMap systemFileTypeMap = FileTypeMap.getDefaultFileTypeMap();
  private final static Map<String, String> defaultContentTypeMap = new HashMap<String, String>();
  private final static Set<String> requiresCharset = new HashSet<String>();

  static {
    defaultContentTypeMap.put("css",  "text/css");
    defaultContentTypeMap.put("html", "text/html");
    defaultContentTypeMap.put("htm",  "text/html");
    defaultContentTypeMap.put("txt",  "text/plain");
    defaultContentTypeMap.put("xml",  "application/xml");
    defaultContentTypeMap.put("js",   "application/javascript");
    defaultContentTypeMap.put("png",  "image/png");
    defaultContentTypeMap.put("gif",  "image/gif");
    defaultContentTypeMap.put("jpg",  "image/jpeg");
    defaultContentTypeMap.put("jpeg", "image/jpeg");

    requiresCharset.add("text/css");
    requiresCharset.add("text/html");
    requiresCharset.add("text/plain");
    requiresCharset.add("application/xml");
    requiresCharset.add("application/javascript");
  }

  /**
   * Returns a valid HTTP contentType's for a given filename. It first relies on the custom defaultContentTypeMap and
   * if not found it will fall back to defaultFileTypeMap from javax.activation.FileTypeMap.
   *
   * Examples:
   *  - "somefile.css" resolves to "text/css".
   *  - "somefile.js.png" resolves to "image/png"
   *  - "/blah/index.html resolves to "text/html"
   *
   * @param fileName with an filename extension
   * @return contentType
   */
  public static String get(final String fileName) {
    String extension = FilenameUtils.getExtension(fileName).toLowerCase();
    if(defaultContentTypeMap.containsKey(extension)) {
      return defaultContentTypeMap.get(extension);
    }
    return systemFileTypeMap.getContentType(fileName);
  }

  public static String get(final String fileName, final   String encoding) {
    final String contentType = get(fileName);
    if(requiresCharset.contains(contentType)) {
      return contentType + "; charset=" + encoding;
    }
    return contentType;
  }
}