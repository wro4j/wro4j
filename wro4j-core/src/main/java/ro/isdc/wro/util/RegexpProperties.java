package ro.isdc.wro.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;


/**
 * Used to overcome the limitation of {@link Properties} class when dealing with regular expressions read from
 * properties file.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 * @created 11 Jun 2012
 */
public class RegexpProperties {
  private Properties properties;
  //\s*(.*)\b\s*?=\s*\b(.*)
  private static final String REGEX_KEY_VALUE = "(?m)\\s*(.*)\\b\\s*?=\\s*\\b(.*)$";
  //private static final String REGEX_KEY_VALUE = "(?m)\\w";
  private static final String REGEX_COMMENTS = "#.*";
  private static final Pattern PATTERN_KEY_VALUE = Pattern.compile(REGEX_KEY_VALUE);
  public RegexpProperties() {
    this(new Properties());
  }
  
  private RegexpProperties(final Properties properties) {
    Validate.notNull(properties);
    this.properties = properties;
  }
  
  public Properties load(final InputStream inputStream) throws IOException {
    Validate.notNull(inputStream);
    final String rawContent = IOUtils.toString(inputStream, CharEncoding.UTF_8);
    readProperties(rawContent.replaceAll(REGEX_COMMENTS, ""));
    return this.properties;
  }

  private void readProperties(final String propertiesAsString) {
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
      System.out.println("add key: " + key);
      System.out.println("add value: " + value);
      this.properties.put(key, value);
      //this.properties.setProperty(key, value);
    }
  }
}
