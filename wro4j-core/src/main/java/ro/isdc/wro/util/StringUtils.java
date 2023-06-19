/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * StringUtils Utility class. This class contains Utility methods for
 * manipulating strings. Inspired from commons and Spring.
 *
 * @author Alex Objelean
 */
public final class StringUtils {
  /**
   * Folder separator.
   */
  private static final String FOLDER_SEPARATOR = "/";

  /**
   * Top path separator.
   */
  private static final String TOP_PATH = "..";

  /**
   * Current path character.
   */
  private static final String CURRENT_PATH = ".";

  /**
   * Windows folder separator.
   */
  private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

  /**
   * Private constructor to prevent instantiation.
   */
  private StringUtils() {}

  /**
   * Normalize the path by suppressing sequences like "path/.." and inner simple
   * dots.
   * <p>
   * The result is convenient for path comparison. For other uses, notice that
   * Windows separators ("\") are replaced by simple slashes.
   *
   * @param path
   *          the original path
   * @return the normalized path
   */
  public static String cleanPath(final String path) {
    if (path == null) {
      return null;
    }
    String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

    // Strip prefix from path to analyze, to not treat it as part of the
    // first path element. This is necessary to correctly parse paths like
    // "file:core/../core/io/Resource.class", where the ".." should just
    // strip the first "core" directory while keeping the "file:" prefix.
    final int prefixIndex = pathToUse.indexOf(":");
    String prefix = EMPTY;
    if (prefixIndex != -1) {
      prefix = pathToUse.substring(0, prefixIndex + 1);
      pathToUse = pathToUse.substring(prefixIndex + 1);
    }
    if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
      prefix = prefix + FOLDER_SEPARATOR;
      pathToUse = pathToUse.substring(1);
    }

    final String[] pathArray = delimitedListToStringArray(pathToUse,
        FOLDER_SEPARATOR);
    final List<String> pathElements = new ArrayList<>();
    int tops = 0;

    for (int i = pathArray.length - 1; i >= 0; i--) {
      final String element = pathArray[i];
      if (CURRENT_PATH.equals(element)) {
        // Points to current directory - drop it.
      } else if (TOP_PATH.equals(element)) {
        // Registering top path found.
        tops++;
      } else {
        if (tops > 0) {
          // Merging path element with element corresponding to top path.
          tops--;
        } else {
          // Normal path element found.
          pathElements.add(0, element);
        }
      }
    }

    // Remaining top paths need to be retained.
    for (int i = 0; i < tops; i++) {
      pathElements.add(0, TOP_PATH);
    }

    return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
  }

  /**
   * Replace all occurrences of a substring within a string with another string.
   *
   * @param inString
   *          String to examine
   * @param oldPattern
   *          String to replace
   * @param newPattern
   *          String to insert
   * @return a String with the replacements
   */
  private static String replace(final String inString, final String oldPattern,
      final String newPattern) {
    if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
      return inString;
    }
    final StringBuilder sbuf = new StringBuilder();
    // output StringBuilder we'll build up
    int pos = 0; // our position in the old string
    int index = inString.indexOf(oldPattern);
    // the index of an occurrence we've found, or -1
    final int patLen = oldPattern.length();
    while (index >= 0) {
      sbuf.append(inString.substring(pos, index));
      sbuf.append(newPattern);
      pos = index + patLen;
      index = inString.indexOf(oldPattern, pos);
    }
    sbuf.append(inString.substring(pos));
    // remember to append any characters to the right of a match
    return sbuf.toString();
  }

  /**
   * Convenience method to return a Collection as a delimited (e.g. CSV) String.
   * E.g. useful for <code>toString()</code> implementations.
   *
   * @param coll
   *          the Collection to display
   * @param delim
   *          the delimiter to use (probably a ",")
   * @return the delimited String
   */
  private static String collectionToDelimitedString(final Collection<String> coll,
      final String delim) {
    return collectionToDelimitedString(coll, delim, EMPTY, EMPTY);
  }

  /**
   * Convenience method to return a Collection as a delimited (e.g. CSV) String.
   * E.g. useful for <code>toString()</code> implementations.
   *
   * @param coll
   *          the Collection to display
   * @param delim
   *          the delimiter to use (probably a ",")
   * @param prefix
   *          the String to start each element with
   * @param suffix
   *          the String to end each element with
   * @return the delimited String
   */
  private static String collectionToDelimitedString(final Collection<String> coll,
      final String delim, final String prefix, final String suffix) {
    if (coll == null || coll.isEmpty()) {
      return EMPTY;
    }
    final StringBuilder sb = new StringBuilder();
    final Iterator<String> it = coll.iterator();
    while (it.hasNext()) {
      sb.append(prefix).append(it.next()).append(suffix);
      if (it.hasNext()) {
        sb.append(delim);
      }
    }
    return sb.toString();
  }

  /**
   * Take a String which is a delimited list and convert it to a String array.
   * <p>
   * A single delimiter can consists of more than one character: It will still
   * be considered as single delimiter string, rather than as bunch of potential
   * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
   *
   * @param str
   *          the input String
   * @param delimiter
   *          the delimiter between elements (this is a single delimiter, rather
   *          than a bunch individual delimiter characters)
   * @return an array of the tokens in the list
   * @see #tokenizeToStringArray
   */
  private static String[] delimitedListToStringArray(final String str,
      final String delimiter) {
    return delimitedListToStringArray(str, delimiter, null);
  }

  /**
   * Take a String which is a delimited list and convert it to a String array.
   * <p>
   * A single delimiter can consists of more than one character: It will still
   * be considered as single delimiter string, rather than as bunch of potential
   * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
   *
   * @param str
   *          the input String
   * @param delimiter
   *          the delimiter between elements (this is a single delimiter, rather
   *          than a bunch individual delimiter characters)
   * @param charsToDelete
   *          a set of characters to delete. Useful for deleting unwanted line
   *          breaks: e.g. "\r\n\f" will delete all new lines and line feeds in
   *          a String.
   * @return an array of the tokens in the list
   * @see #tokenizeToStringArray
   */
  private static String[] delimitedListToStringArray(final String str,
      final String delimiter, final String charsToDelete) {
    if (str == null) {
      return new String[0];
    }
    if (delimiter == null) {
      return new String[] { str };
    }
    final List<String> result = new ArrayList<String>();
    if ("".equals(delimiter)) {
      for (int i = 0; i < str.length(); i++) {
        result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
      }
    } else {
      int pos = 0;
      int delPos = 0;
      while ((delPos = str.indexOf(delimiter, pos)) != -1) {
        result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
        pos = delPos + delimiter.length();
      }
      if (str.length() > 0 && pos <= str.length()) {
        // Add rest of String, but not in case of empty input.
        result.add(deleteAny(str.substring(pos), charsToDelete));
      }
    }
    return toStringArray(result);
  }

  /**
   * Copy the given Collection into a String array. The Collection must contain
   * String elements only.
   *
   * @param collection
   *          the Collection to copy
   * @return the String array (<code>null</code> if the passed-in Collection
   *         was <code>null</code>)
   */
  private static String[] toStringArray(final Collection<?> collection) {
    if (collection == null) {
      return null;
    }
    return collection.toArray(new String[collection.size()]);
  }

  /**
   * Delete any character in a given String.
   *
   * @param inString
   *          the original String
   * @param charsToDelete
   *          a set of characters to delete. E.g. "az\n" will delete 'a's, 'z's
   *          and new lines.
   * @return the resulting String
   */
  private static String deleteAny(final String inString,
      final String charsToDelete) {
    if (!hasLength(inString) || !hasLength(charsToDelete)) {
      return inString;
    }
    final StringBuilder out = new StringBuilder();
    for (int i = 0; i < inString.length(); i++) {
      final char c = inString.charAt(i);
      if (charsToDelete.indexOf(c) == -1) {
        out.append(c);
      }
    }
    return out.toString();
  }

  /**
   * Check that the given CharSequence is neither <code>null</code> nor of
   * length 0. Note: Will return <code>true</code> for a CharSequence that
   * purely consists of whitespace.
   * <p>
   *
   * <pre>
   * StringUtils.hasLength(null) = false
   * StringUtils.hasLength(&quot;&quot;) = false
   * StringUtils.hasLength(&quot; &quot;) = true
   * StringUtils.hasLength(&quot;Hello&quot;) = true
   * </pre>
   *
   * @param str
   *          the CharSequence to check (may be <code>null</code>)
   * @return <code>true</code> if the CharSequence is not null and has length
   * @see #hasText(String)
   */
  private static boolean hasLength(final CharSequence str) {
    return (str != null && str.length() > 0);
  }

  /**
   * Check that the given String is neither <code>null</code> nor of length 0.
   * Note: Will return <code>true</code> for a String that purely consists of
   * whitespace.
   *
   * @param str
   *          the String to check (may be <code>null</code>)
   * @return <code>true</code> if the String is not null and has length
   * @see #hasLength(CharSequence)
   */
  private static boolean hasLength(final String str) {
    return hasLength((CharSequence) str);
  }
}
