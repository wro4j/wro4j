package ro.isdc.wro.model.resource.processor.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.http.support.ContentTypeResolver;
import ro.isdc.wro.util.Base64;


/**
 * Generator for Data URIs.
 *
 * (Inspired from: http://github.com/nzakas/cssembed.)
 * 
 * @author Alex Objelean
 * @author Ivar Conradi Østhus
 */
public class DataUriGenerator {
  private static final String DATA_URI_PREFIX = "data:";
  
  /**
   * Generate the dataUri as string associated to the passed InputStream with encoding and type based on provided
   * fileName.
   */
  public String generateDataURI(final InputStream inputStream, final String fileName)
      throws IOException {
    final StringWriter writer = new StringWriter();
    final byte[] bytes = IOUtils.toByteArray(inputStream);
    inputStream.close();

    final String mimeType = getMimeType(fileName);
    // actually write
    generateDataURI(bytes, writer, mimeType);

    return writer.toString();
  }

  private String getMimeType(final String fileName) {
    return ContentTypeResolver.get(fileName, StandardCharsets.UTF_8.name()).replaceAll(" ", StringUtils.EMPTY);
  }

  /**
   * Check if the url is actually a dataUri (base64 encoded value).
   * 
   * @param url
   *          to check
   * @return true if the url is a base64 encoded value.
   */
  public static boolean isDataUri(final String url) {
    return url.startsWith(DATA_URI_PREFIX);
  }
  
  /**
   * Generates a data URI from a byte array and outputs to the given writer.
   * 
   * @param bytes
   *          The array of bytes to output to the data URI.
   * @param out
   *          Where to output the data URI.
   * @param mimeType
   *          The MIME type to specify in the data URI.
   * @throws java.io.IOException
   */
  private void generateDataURI(final byte[] bytes, final Writer out, final String mimeType)
      throws IOException {
    // create the output
    final StringBuffer buffer = new StringBuffer();
    buffer.append(DATA_URI_PREFIX);
    
    // add MIME type
    buffer.append(mimeType);
    
    // output base64-encoding
    buffer.append(";base64,");
    buffer.append(Base64.encodeBytes(bytes));
    
    // output to writer
    out.write(buffer.toString());
  }
}
