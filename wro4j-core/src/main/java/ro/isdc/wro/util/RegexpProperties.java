package ro.isdc.wro.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;


/**
 * Used to overcome the limitation of {@link Properties} class when dealing with regular expressions read from
 * properties file. The main benefit of using {@link RegexpProperties} over simple {@link Properties} is that the regexp
 * doesn't have to be escaped.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class RegexpProperties {
  private final Properties properties;
  private static final String REGEX_KEY_VALUE = "(?m)\\s*(.*?)\\s*=\\s*(.*)\\s*$";
  private static final String REGEX_COMMENTS = "#.*";
  private static final Pattern PATTERN_KEY_VALUE = Pattern.compile(REGEX_KEY_VALUE);

  public RegexpProperties() {
    this.properties = new Properties();
  }
  
  /**
   * Load the properties from the stream. The implementation will handle comments properly by removing them before
   * properties are loaded.
   * 
   * @param inputStream
   * @return {@link Properties} containing properties parsed from the stream.
   * @throws IOException
   */
  public Properties load(final InputStream inputStream) throws IOException {
    Validate.notNull(inputStream);
    final String rawContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    parseProperties(rawContent.replaceAll(REGEX_COMMENTS, ""));
    return this.properties;
  }

  /**
   * parse the properties from the provided string containing a raw properties 
   */
  private void parseProperties(final String propertiesAsString) {
    //should work also \r?\n
    final String[] propertyEntries = propertiesAsString.split("\\r?\\n");
    for (final String entry : propertyEntries) {
      readPropertyEntry(entry);
    }
  }

  private void readPropertyEntry(final String entry) {
    Matcher matcher = PATTERN_KEY_VALUE.matcher(entry);
    while(matcher.find()) {
      final String key = matcher.group(1);
      final String value = matcher.group(2);
      this.properties.put(key, value);
    }
  }
}
