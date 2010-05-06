/*
 * Copyright (C) 2010 Betfair. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.algorithm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


/**
 * Generator for Data URIs. Inspired from: http://github.com/nzakas/cssembed.
 *
 * @author Alex Objelean
 */
public class DataUriGenerator {
  private final Map<String, String> binaryTypes = new HashMap<String, String>();
  private final Map<String, String> textTypes = new HashMap<String, String>();
  private final boolean verbose = false;

  public DataUriGenerator() {
    initTypes();
  }

  public void initTypes() {
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
   *
   * @param inputStream
   * @param out
   * @param fileName
   * @param mimeType
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void generateDataURI(final InputStream inputStream, final Writer out, final String fileName, String mimeType)
      throws IOException {
    // read the bytes from the file
    final byte[] bytes = IOUtils.toByteArray(inputStream);
    inputStream.close();

    // verify MIME type and charset
    mimeType = getMimeType(fileName, mimeType);
    // actually write
    generateDataURI(bytes, out, mimeType);
  }

  public void generateDataURI(final InputStream inputStream, final Writer out, final String fileName)
      throws IOException {
    // read the bytes from the file
    final byte[] bytes = IOUtils.toByteArray(inputStream);
    inputStream.close();

    // verify MIME type and charset
    final String mimeType = getMimeType(fileName, null);
    // actually write
    generateDataURI(bytes, out, mimeType);
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
   * @param charset
   *          The character set to specify in the data URI.
   * @throws java.io.IOException
   */
  private static void generateDataURI(final byte[] bytes, final Writer out, final String mimeType)
      throws IOException {
    // create the output
    final StringBuffer buffer = new StringBuffer();
    buffer.append("data:");

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
   * @param filename
   *          The filename to check.
   * @param mimeType
   *          The provided MIME type or null if nothing was provided.
   * @return The MIME type string to use for the filename.
   * @throws java.io.IOException
   *           When no MIME type can be determined.
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
      if (verbose) {
        System.err.println("[INFO] No MIME type provided, defaulting to '" + mimeType + "'.");
      }
    }
    return mimeType;
  }
}
