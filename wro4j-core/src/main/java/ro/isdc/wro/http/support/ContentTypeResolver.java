package ro.isdc.wro.http.support;

import javax.activation.FileTypeMap;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeResolver {

  private final static FileTypeMap systemFileTypeMap = FileTypeMap.getDefaultFileTypeMap();
  private final static Map<String, String> defaultContentTypeMap = new HashMap<String, String>();

  static {
    defaultContentTypeMap.put(".css", "text/css");
    defaultContentTypeMap.put(".js", "application/javascript");
    defaultContentTypeMap.put(".png", "image/png");
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
  public static String get(String fileName) {
    int pos = fileName.lastIndexOf(".");
    String extension = fileName.substring(pos);

    if(defaultContentTypeMap.containsKey(extension)) {
      return defaultContentTypeMap.get(extension);
    }

    return systemFileTypeMap.getContentType(fileName);
  }
}