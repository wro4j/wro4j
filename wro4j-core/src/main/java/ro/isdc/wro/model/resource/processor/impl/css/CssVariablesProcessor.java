/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * Processor of css resources, responsible for replacing variables. (@see
 * http://disruptive-innovations.com/zoo/cssvariables/). This is a pre processor, because it makes sense to apply
 * variables only on the same css. <br/>
 * This processor is implemented as both: preprocessor and postprocessor.
 * 
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class CssVariablesProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssVariablesProcessor.class);
  public static final String ALIAS = "cssVariables";
  /**
   * Compiled pattern for REGEX_VARIABLES_DEFINITION regex.
   */
  private static final Pattern PATTERN_VARIABLES_DEFINITION = Pattern.compile(WroUtil.loadRegexpWithKey("cssVariables.definition"));
  /**
   * Compiled pattern for REGEX_VARIABLES_BODY pattern.
   */
  private static final Pattern PATTERN_VARIABLES_BODY = Pattern.compile(WroUtil.loadRegexpWithKey("cssVariables.body"));
  /**
   * Compiled pattern for REGEX_VARIABLE_HOLDER pattern.
   */
  private static final Pattern PATTERN_VARIABLE_HOLDER = Pattern.compile(WroUtil.loadRegexpWithKey("cssVariables.holder"));

  /**
   * Extract variables map from variables body.
   * 
   * @param variablesBody
   *          string containing variables mappings.
   * @return map with extracted variables.
   */
  private Map<String, String> extractVariables(final String variablesBody) {
    final Map<String, String> map = new HashMap<String, String>();
    final Matcher m = PATTERN_VARIABLES_BODY.matcher(variablesBody);
    LOG.debug("parsing variables body");
    while (m.find()) {
      final String key = m.group(1);
      final String value = m.group(2);
      if (map.containsKey(key)) {
        LOG.warn("A duplicate variable name found with name: {} and value: {}.", key, value);
      }
      map.put(key, value);
    }
    return map;
  }

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
    try {
      final String css = IOUtils.toString(reader);
      final String result = parseCss(css);
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Parse css, find all defined variables & replace them.
   * 
   * @param css
   *          to parse.
   */
  private String parseCss(final String css) {
    // map containing variables & their values
    final Map<String, String> map = new HashMap<String, String>();
    final StringBuffer sb = new StringBuffer();
    final Matcher m = PATTERN_VARIABLES_DEFINITION.matcher(css);
    while (m.find()) {
      final String variablesBody = m.group(1);
      // LOG.debug("variables body: " + variablesBody);
      // extract variables
      map.putAll(extractVariables(variablesBody));
      // remove variables definition
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);

    // LOG.debug("replaced variables: " + result);
    return replaceVariables(sb.toString(), map);
  }

  /**
   * Replace variables from css with provided variables map.
   * 
   * @param cleanCss
   *          used to replace variable placeholders.
   * @param variables
   *          map of variables used for substitution.
   * @return css with all variables replaced.
   */
  private String replaceVariables(final String css, final Map<String, String> variables) {
    final StringBuffer sb = new StringBuffer();
    final Matcher m = PATTERN_VARIABLE_HOLDER.matcher(css);
    while (m.find()) {
      final String oldMatch = m.group();
      final String variableName = m.group(1);
      final String variableValue = variables.get(variableName);
      if (variableValue != null) {
        final String newReplacement = oldMatch.replace(oldMatch, variableValue);
        m.appendReplacement(sb, newReplacement.trim());
      } else {
        LOG.warn("No variable with name " + variableName + " was found!");
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
