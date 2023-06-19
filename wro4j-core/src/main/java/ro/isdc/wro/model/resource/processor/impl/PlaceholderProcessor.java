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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * A processor which parse a resource and search for placeholders of this type: <code>${}</code> and replace them with
 * the values found in a map provided the by client.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public class PlaceholderProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(PlaceholderProcessor.class);
  private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile(WroUtil.loadRegexpWithKey("placeholder"));
  private static final Properties EMPTY_PROPERTIES = new Properties();

  /**
   * Factory used to build Properties object containing values of the variables to substitute.
   */
  private ObjectFactory<Properties> propertiesFactory;

  /**
   * If false, when a variable is not defined, a runtime exception will be thrown. Default value is true - meaning that
   * missing variables will be replaced with empty value.
   */
  private boolean ignoreMissingVariables = true;

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final Matcher matcher = PATTERN_PLACEHOLDER.matcher(content);
    final StringBuffer sb = new StringBuffer();

    Properties properties = null;
    if (propertiesFactory != null) {
      properties = propertiesFactory.create();
    }
    //be sure that properties will never be null;
    if (properties == null) {
      properties = EMPTY_PROPERTIES;
    }
    while (matcher.find()) {
      final String variableName = matcher.group(1);
      LOG.debug("found placeholder: {}", variableName);
      matcher.appendReplacement(sb, replaceVariable(properties, variableName));
    }
    matcher.appendTail(sb);
    writer.write(sb.toString());
  }


  /**
   * @param variableName
   * @return
   */
  private String replaceVariable(final Properties properties, final String variableName) {
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
  public PlaceholderProcessor setIgnoreMissingVariables(final boolean ignoreMissingVariables) {
    this.ignoreMissingVariables = ignoreMissingVariables;
    return this;
  }


  /**
   * @param propertiesFactory the propertiesFactory to set
   */
  public PlaceholderProcessor setPropertiesFactory(final ObjectFactory<Properties> propertiesFactory) {
    this.propertiesFactory = propertiesFactory;
    return this;
  }
}
