/*
 * Copyright (c) 2009 Nicholas C. Zakas. All rights reserved. http://www.nczonline.net/ Permission is hereby granted,
 * free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.nczonline.web.datauri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;


/**
 * Generator for Data URIs.
 *
 * @author Nicholas C. Zakas
 */
public class DataURIGenerator {

  private static HashMap binaryTypes = new HashMap();
  private static HashMap textTypes = new HashMap();
  private static boolean verbose = false;

  // initialize file types and MIME types
  static {
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

  // --------------------------------------------------------------------------
  // Get/Set verbose flag
  // --------------------------------------------------------------------------

  public static boolean getVerbose() {
    return verbose;
  }

  public static void setVerbose(final boolean newVerbose) {
    verbose = newVerbose;
  }

  // --------------------------------------------------------------------------
  // Generate data URIs from a file
  // --------------------------------------------------------------------------

  /**
   * Generates a data URI from a file, outputting it to the given writer. The MIME type is determined from examining the
   * filename.
   *
   * @param file
   *          The file from which to generate the data URI.
   * @param out
   *          Where to output the data URI.
   * @throws java.io.IOException
   */
  public static void generate(final File file, final Writer out)
      throws IOException {
    generate(file, out, null);
  }

  /**
   * Generates a data URI from a file, outputting it to the given writer.
   *
   * @param file
   *          The file from which to generate the data URI.
   * @param out
   *          Where to output the data URI.
   * @param mimeType
   *          The MIME type to use for the data URI.
   * @throws java.io.IOException
   */
  public static void generate(final File file, final Writer out, final String mimeType)
      throws IOException {
    generateDataURI(file, out, mimeType);
  }

  // --------------------------------------------------------------------------
  // Generate data URIs from a URL
  // --------------------------------------------------------------------------

  /**
   * Generates a data URI from a file, outputting it to the given writer. The MIME type is determined from examining the
   * filename.
   *
   * @param file
   *          The file from which to generate the data URI.
   * @param out
   *          Where to output the data URI.
   * @throws java.io.IOException
   */
  public static void generate(final URL url, final Writer out)
      throws IOException {
    generate(url, out, null);
  }

  /**
   * Generates a data URI from a URL, outputting it to the given writer.
   *
   * @param url
   *          The URL form which to generate the data URI.
   * @param out
   *          Where to output the data URI.
   * @param mimeType
   *          The MIME type to use for the data URI.
   * @throws java.io.IOException
   */
  public static void generate(final URL url, final Writer out, final String mimeType)
      throws IOException {
    generateDataURI(url, out, mimeType);
  }

  // --------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------

  /**
   * Generates a data URI from the specified file and outputs to the given writer.
   *
   * @param file
   *          The file to from which to create a data URI.
   * @param out
   *          Where to output the data URI.
   * @param mimeType
   *          The MIME type to specify in the data URI.
   * @param charset
   *          The character set to specify in the data URI.
   * @throws java.io.IOException
   */
  private static void generateDataURI(final File file, final Writer out, final String mimeType)
      throws IOException {
    generateDataURI(new FileInputStream(file), out, file.getName(), mimeType);
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
  public static void generateDataURI(final InputStream inputStream, final Writer out, final String fileName, String mimeType)
      throws FileNotFoundException, IOException {
    // read the bytes from the file
    final byte[] bytes = IOUtils.toByteArray(inputStream);
    inputStream.close();

    // verify MIME type and charset
    mimeType = getMimeType(fileName, mimeType);
    // actually write
    generateDataURI(bytes, out, mimeType);
  }

  /**
   * Generates a data URI from the specified URL and outputs to the given writer.
   *
   * @param url
   *          The URL to from which to create a data URI.
   * @param out
   *          Where to output the data URI.
   * @param mimeType
   *          The MIME type to specify in the data URI.
   * @throws java.io.IOException
   */
  private static void generateDataURI(final URL url, final Writer out, String mimeType)
      throws IOException {

    // get information about the URL
    final URLConnection conn = url.openConnection();

    // if no MIME type has been specified, get from the connection
    if (mimeType == null) {
      mimeType = getMimeType(url.getFile(), conn.getContentType());
      if (verbose) {
        System.err.println("[INFO] No MIME type provided, using detected type of '" + mimeType + "'.");
      }
    }

    // sometimes charset is in the MIME type
    if (mimeType.indexOf("; charset=") > -1) {
      mimeType = mimeType.replace(" ", ""); // remove the space
    } else {
      mimeType = getMimeTypeWithCharset(mimeType);
    }

    // read the bytes from the URL
    final InputStream in = conn.getInputStream();
    final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    int c;

    while ((c = in.read()) != -1) {
      byteStream.write(c);
    }

    byteStream.flush();
    in.close();

    // actually write
    generateDataURI(byteStream.toByteArray(), out, mimeType);
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
   * Determines if the given filename represents an image file.
   *
   * @param filename
   *          The filename to check.
   * @return True if the filename represents an image, false if not.
   */
  private static boolean isImageFile(final String filename) {
    final String fileType = getFileType(filename);
    return binaryTypes.containsKey(fileType) && binaryTypes.get(fileType).toString().startsWith("image");
  }

  /**
   * Retrieves the extension for the filename.
   *
   * @param filename
   *          The filename to get the extension from.
   * @return All characters after the final "." in the filename.
   */
  private static String getFileType(final String filename) {
    String type = "";

    final int idx = filename.lastIndexOf('.');
    if (idx >= 0 && idx < filename.length() - 1) {
      type = filename.substring(idx + 1);
    }

    return type;
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
  private static String getMimeType(final String filename, String mimeType)
      throws IOException {
    if (mimeType == null) {

      final String type = getFileType(filename);

      // if it's an image type, don't use a charset
      if (binaryTypes.containsKey(type)) {
        mimeType = (String) binaryTypes.get(type);
      } else if (textTypes.containsKey(type)) {
        mimeType = (String) textTypes.get(type) + ";charset=UTF-8";
      } else {
        throw new IOException("No MIME type provided and MIME type couldn't be automatically determined.");
      }

      if (verbose) {
        System.err.println("[INFO] No MIME type provided, defaulting to '" + mimeType + "'.");
      }
    }

    return mimeType;
  }

  private static String getMimeTypeWithCharset(final String mimeType) {

    if (binaryTypes.containsValue(mimeType)) {
      if (verbose) {
        System.err.println("[INFO] Image file detected, skipping charset.");
      }
      return mimeType;
    } else {
      if (verbose) {
        System.err.println("[INFO] Using charset 'UTF-8'.");
      }
      return mimeType + ";charset=UTF-8";
    }

  }

}
