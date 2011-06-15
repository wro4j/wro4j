/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * A processor which parse a resource and search for placeholders of this type: <code>${}</code> and replace them with
 * the values found in a map provided the by client.
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 15 Jun 2011
 */
public class PlaceholderInterpolationProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(PlaceholderInterpolationProcessor.class);

  /** The url pattern */
  private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("(?ims)\\$\\{((.*?))}");
  /**
   * Properties containing values of the variables to substitute. By default use an empty properties object.
   */
  private Properties properties = new Properties();
  /**
   * If false, when a variable is not defined, a runtime exception will be thrown. Default value is true - meaning that
   * missing variables will be replaced with empty value.
   */
  private boolean ignoreMissingVariables = true;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final Matcher matcher = PATTERN_PLACEHOLDER.matcher(content);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      final String variableName = matcher.group(1);
      LOG.debug("found placeholder: {}", variableName);
      matcher.appendReplacement(sb, replaceVariable(variableName));
    }
    matcher.appendTail(sb);
    writer.write(sb.toString());
  }


  /**
   * @param variableName
   * @return
   */
  private String replaceVariable(final String variableName) {
    final String variableValue = properties.getProperty(variableName);
    if (!ignoreMissingVariables && variableValue == null) {
      throw new WroRuntimeException("No value defind for variable called: [" + variableName + "]");
    }

    final String result = variableValue == null ? StringUtils.EMPTY : variableValue;
    LOG.debug("replacing: [{}] with [{}]", variableName, result);
    return result;
  }


  /**
   * @param ignoreMissingVariables the ignoreMissingVariables to set
   */
  public PlaceholderInterpolationProcessor setIgnoreMissingVariables(final boolean ignoreMissingVariables) {
    this.ignoreMissingVariables = ignoreMissingVariables;
    return this;
  }


  /**
   * @param properties the properties to set
   */
  public PlaceholderInterpolationProcessor setProperties(final Properties properties) {
    LOG.debug("setting properties: {}", properties);
    //be sure that properties will never be null;
    if (properties != null) {
      this.properties = properties;
    }
    return this;
  }
}
