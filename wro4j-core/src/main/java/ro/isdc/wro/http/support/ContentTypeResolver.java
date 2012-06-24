package ro.isdc.wro.http.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.activation.FileTypeMap;

import org.apache.commons.io.FilenameUtils;

public class ContentTypeResolver {

  private final static FileTypeMap systemFileTypeMap = FileTypeMap.getDefaultFileTypeMap();
  private final static Map<String, String> defaultContentTypeMap = new HashMap<String, String>();
  private final static Set<String> requiresCharset = new HashSet<String>();

  static {
    defaultContentTypeMap.put("txt",   "text/plain");
    defaultContentTypeMap.put("css",   "text/css");
    defaultContentTypeMap.put("html",  "text/html");
    defaultContentTypeMap.put("htm",   "text/html");
    defaultContentTypeMap.put("xml",   "application/xml");
    defaultContentTypeMap.put("xhtml", "application/xhtml+xml");
    defaultContentTypeMap.put("js",    "application/javascript");
    defaultContentTypeMap.put("png",   "image/png");
    defaultContentTypeMap.put("gif",   "image/gif");
    defaultContentTypeMap.put("jpg",   "image/jpeg");
    defaultContentTypeMap.put("jpeg",  "image/jpeg");
    //font types
    defaultContentTypeMap.put("eot", "application/vnd.ms-fontobject");
    defaultContentTypeMap.put("otf", "application/x-font-opentype");
    defaultContentTypeMap.put("ttf", "application/octet-stream");


    requiresCharset.add("text/css");
    requiresCharset.add("text/html");
    requiresCharset.add("text/plain");
    requiresCharset.add("application/xml");
    requiresCharset.add("application/xhtml+xml");
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

  /**
   * Returns a valid HTTP contentType's for a given filename with charset. It first relies on the custom
   * defaultContentTypeMap and if not found it will fall back to defaultFileTypeMap from javax.activation.FileTypeMap.
   *
   * Examples:
   * - ("somefile.css", "UTF-8") resolves to "text/css"; charset=UTF-8".
   * - ("somefile.js.png", "UTF-8") resolves to "image/png"
   * - ("/blah/index.html, "UTF-8") resolves to "text/html; charset=8"
   * 
   * @param fileName
   *          with an filename extension
   * @param encoding
   *          which encoding to use
   * @return contentType
   */
  public static String get(final String fileName, final   String encoding) {
    final String contentType = get(fileName);
    if(requiresCharset.contains(contentType)) {
      return contentType + "; charset=" + encoding;
    }
    return contentType;
  }
}