/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * Css minify barryvan implementation.
 */
public class CSSMin {
  private static final Logger LOG = LoggerFactory.getLogger(CSSMin.class);


  public void formatFile(final String input, final Writer writer)
    throws Exception {
    try {
      final StringBuilder sb = new StringBuilder(input.replaceAll("[\t\n\r]", "").replaceAll("  ", " "));
      int k, n;

      // Find the start of the comment
      while ((n = sb.indexOf("/*")) != -1) {
        k = sb.indexOf("*/", n + 2);
        if (k == -1) {
          throw new Exception("Error: Unterminated comment.");
        }
        sb.delete(n, k + 2);
      }

      final List<Selector> selectors = new LinkedList<Selector>();
      n = 0;
      while ((k = sb.indexOf("}", n)) != -1) {
        try {
          selectors.add(new Selector(sb.substring(n, k + 1)));
        } catch (final Exception e) {
          // skip
        }
        n = k + 1;
      }

        for (Selector selector : selectors) {
            writer.write(selector.toString());
        }
      writer.write("\r\n");
      writer.flush();
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
    }

  }
}


class Selector {
  private static final Logger LOG = LoggerFactory.getLogger(CSSMin.class);
  private final Property[] properties;
  private final String selector;


  /**
   * Creates a new Selector using the supplied strings.
   *
   * @param selector The selector; for example, "div { border: solid 1px red; color: blue; }"
   */
  public Selector(final String selector) throws Exception {
    final String[] parts = selector.split("\\{"); // We have to escape the { with a \ for the regex, which itself
    // requires escaping for the string. Sigh.
    if (parts.length < 2) {
      throw new Exception("Warning: Incomplete selector: " + selector);
    }
    this.selector = parts[0].trim();
    String contents = parts[1].trim();
    if (contents.length() <= 1) {
      throw new Exception("Warning: Empty selector body: " + selector);
    }
    if (contents.charAt(contents.length() - 1) != '}') { // Ensure that we have a leading and trailing brace.
      throw new Exception("Warning: Unterminated selector: " + selector);
    }
    contents = StringUtils.substringBefore(contents, "}");
    properties = parseProperties(contents);
    sortProperties(properties);
  }


  /**
   * Prints out this selector and its contents nicely, with the contents sorted alphabetically.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(selector).append("{");
    for (final Property property : properties) {
      if (property != null) {
        sb.append(property.toString());
      }
    }
    sb.append("}");
    return sb.toString();
  }


  /**
   * Parses out the properties of a selector's body.
   *
   * @param contents The body; for example, "border: solid 1px red; color: blue;"
   */
  private Property[] parseProperties(final String contents) {
    final String[] parts = contents.split(";");
    final List<Property> resultsAsList = new ArrayList<Property>();

      for (String part : parts) {
          try {
              // ignore empty parts
              if (!StringUtils.isEmpty(part.trim())) {
                  resultsAsList.add(new Property(part));
              }
          } catch (final Exception e) {
              LOG.warn(e.getMessage(), e);
          }
      }
    return resultsAsList.toArray(new Property[resultsAsList.size()]);
  }


  private void sortProperties(final Property[] propertiesToSort) {
    Arrays.sort(propertiesToSort);
  }
}


class Property
  implements Comparable {
  private static final Logger LOG = LoggerFactory.getLogger(CSSMin.class);
  protected String property;
  protected Value[] values;


  /**
   * Creates a new Property using the supplied strings. Parses out the values of the property selector.
   *
   * @param property The property; for example, "border: solid 1px red;" or
   *        "-moz-box-shadow: 3px 3px 3px rgba(255, 255, 0, 0.5);".
   */
  public Property(final String property) throws Exception {
    try {
      // Parse the property.
      //final String[] parts = property.split(":"); // Split "color: red" to ["color", " red"]
      final List<String> parts = Arrays.asList(property.split(":", 2)); // Split "color: red" to ["color", " red"]
      final List<String> nonEmptyParts = new ArrayList<String>();
      for (String part : parts) {
        if (!StringUtils.isEmpty(part.trim())) {
          nonEmptyParts.add(part.trim());
        }
      }
      if (nonEmptyParts.size() < 2) {
        throw new Exception("Warning: Incomplete property: " + property);
      }
      this.property = nonEmptyParts.get(0).toLowerCase();

      values = parseValues(nonEmptyParts.get(1).replaceAll(", ", ","));

    } catch (final PatternSyntaxException e) {
      // Invalid regular expression used.
    }
  }


  /**
   * Prints out this property nicely, with the contents sorted in a standardised order.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(property).append(":");
    for (final Value v : values) {
      sb.append(v.toString()).append(",");
    }
    sb.deleteCharAt(sb.length() - 1); // Delete the trailing comma.
    sb.append(";");
    return sb.toString();
  }


  public int compareTo(final Object other) {
    return property.compareTo(((Property)other).property);
  }


  private Value[] parseValues(final String contents) {
    final String[] parts = contents.split(",");
    final Value[] results = new Value[parts.length];

    for (int i = 0; i < parts.length; i++) {
      try {
        results[i] = new Value(parts[i]);
      } catch (final Exception e) {
        LOG.error(e.getMessage(), e);
        results[i] = null;
      }
    }

    return results;
  }
}


class Value {

  String[] parts;


  public Value(final String value) throws Exception {
    // Parse the value.
    parts = value.split(" "); // Split "solid 1px red" to ["solid","1px","red"] and sort them.
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final String part : parts) {
      sb.append(part).append(" ");
    }
    return sb.toString().trim();
  }
}
