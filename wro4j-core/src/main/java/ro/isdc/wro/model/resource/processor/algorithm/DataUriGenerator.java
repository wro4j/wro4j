/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generator for Data URIs. Inspired from: http://github.com/nzakas/cssembed.
 *
 * @author Alex Objelean
 * @created 7 May, 2010
 */
public class DataUriGenerator {
  /**
   *
   */
  private static final String DATA_URI_PREFIX = "data:";
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataUriGenerator.class);
  private final Map<String, String> binaryTypes = new HashMap<String, String>();
  private final Map<String, String> textTypes = new HashMap<String, String>();

  /**
   * Default constructor.
   */
  public DataUriGenerator() {
    initTypes();
  }

  /**
   * Generate the dataUri as string associated to the passed InputStream with encoding & type based on provided fileName.
   */
  public String generateDataURI(final InputStream inputStream, final String fileName)
    throws IOException {
    final StringWriter writer = new StringWriter();
    // actually write
    generateDataURI(inputStream, writer, fileName);
    return writer.toString();
  }

  /**
   * Check if the url is actually a dataUri (base64 encoded value).
   * @param url to check
   * @return true if the url is a base64 encoded value.
   */
  public static boolean isDataUri(final String url) {
    return url.startsWith(DATA_URI_PREFIX);
  }

  /**
   * Initialize types.
   */
  private void initTypes() {
    binaryTypes.put("gif", "image/gif");
    binaryTypes.put("jpg", "image/jpeg");
    binaryTypes.put("png", "image/png");
    binaryTypes.put("jpeg", "image/jpeg");

    textTypes.put("htm", "text/html");
    textTypes.put("html", "text/html");
    textTypes.put("xml", "application/xml");
    textTypes.put("xhtml", "application/xhtml+xml");
    textTypes.put("js", "application/x-javascript");
    textTypes.put("css", "text/css");
    textTypes.put("txt", "text/plain");
  }

  /**
   * Generates DataURI based on provided InputStream, fileName and mimeType.
   */
  private void generateDataURI(final InputStream inputStream, final Writer out, final String fileName, String mimeType)
    throws IOException {
    // read the bytes from the file
    final byte[] bytes = IOUtils.toByteArray(inputStream);
    inputStream.close();

    // verify MIME type and charset
    mimeType = getMimeType(fileName, mimeType);
    // actually write
    generateDataURI(bytes, out, mimeType);
  }

  /**
   * Generates dataUri without specifying the mimeType -  this one being guessed from the fileName.
   */
  private void generateDataURI(final InputStream inputStream, final Writer out, final String fileName)
    throws IOException {
    generateDataURI(inputStream, out, fileName, null);
  }


  /**
   * Generates a data URI from a byte array and outputs to the given writer.
   *
   * @param bytes The array of bytes to output to the data URI.
   * @param out Where to output the data URI.
   * @param mimeType The MIME type to specify in the data URI.
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
    buffer.append(new String(Base64.encodeBytes(bytes)));

    // output to writer
    out.write(buffer.toString());
  }


  /**
   * Determines the MIME type to use for the given filename. If a MIME type is passed in, then that is used by default.
   * Otherwise, the filename is inspected to determine the appropriate MIME type.
   *
   * @param filename The filename to check.
   * @param mimeType The provided MIME type or null if nothing was provided.
   * @return The MIME type string to use for the filename.
   * @throws java.io.IOException When no MIME type can be determined.
   */
  private String getMimeType(final String filename, String mimeType)
    throws IOException {
    if (mimeType == null) {
      final String type = FilenameUtils.getExtension(filename);
      // if it's an image type, don't use a charset
      if (binaryTypes.containsKey(type)) {
        mimeType = binaryTypes.get(type);
      } else if (textTypes.containsKey(type)) {
        mimeType = textTypes.get(type) + ";charset=UTF-8";
      } else {
        throw new IOException("No MIME type provided and MIME type couldn't be automatically determined.");
      }
      LOG.warn("[INFO] No MIME type provided, defaulting to '" + mimeType + "'.");
    }
    return mimeType;
  }
}
