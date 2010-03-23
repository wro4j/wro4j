/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.model.resource.processor.impl;

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


/**
 * Processor of css resources, responsible for replacing variables. (@see
 * http://disruptive-innovations.com/zoo/cssvariables/). This is a pre processor, because it makes sense to apply
 * variables only on the same css. <br/>
 * This processor is implemented as both: preprocessor & postprocessor.
 *
 * @author alexandru.objelean
 * @created Created on Jul 05, 2009
 */
@SupportedResourceType(ResourceType.CSS)
public class CssVariablesProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssVariablesProcessor.class);
  /**
   * Pattern used to find variables definition. For instance:<br/>
   * <code>
   *   @variables {
   *     var1: white;
   *     var2: #fff;
   *   }
   * </code>
   */
  private static final String REGEX_VARIABLES_DEFINITION = "@variables\\s*\\{(.*?)\\}";
  /**
   * Pattern used to parse variables body & to extract mapping between variable & its value. For instance:<br/>
   * <code>
   *   corporateLogo: url(test.png);
   *   mainBackground: yellow;
   * </code>
   */
  private static final String REGEX_VARIABLES_BODY = "([^:\\s]*)\\s*:\\s*(.+?);";//"(\\w+)\\s*:\\s*(.+?);";//"(\\w+)\\s*:\\s*([^;]*)";//"(\\w+)\\s*:\\s*(.+)(?:;)";;
  /**
   * Pattern used to parse variables body & to extract mapping between variable & its value. For instance:<br/>
   * <code>
   *   var(corporateLogo);
   * </code>
   */
  private static final String REGEX_VARIABLE_HOLDER = "var\\s*\\((.+?)\\)";//"var\\s*\\((.+?)\\);";

  /**
   * Compiled pattern for REGEX_VARIABLES_DEFINITION regex.
   */
  private static final Pattern PATTERN_VARIABLES_DEFINITION = Pattern.compile(REGEX_VARIABLES_DEFINITION,
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  /**
   * Compiled pattern for REGEX_VARIABLES_BODY pattern.
   */
  private static final Pattern PATTERN_VARIABLES_BODY = Pattern.compile(REGEX_VARIABLES_BODY,
      Pattern.CASE_INSENSITIVE);
  /**
   * Compiled pattern for REGEX_VARIABLE_HOLDER pattern.
   */
  private static final Pattern PATTERN_VARIABLE_HOLDER = Pattern.compile(REGEX_VARIABLE_HOLDER,
      Pattern.CASE_INSENSITIVE);

  /**
   * Extract variables map from variables body.
   * @param variablesBody string containing variables mappings.
   * @return map with extracted variables.
   */
  private Map<String, String> extractVariables(final String variablesBody) {
    final Map<String, String> map = new HashMap<String, String>();
    final Matcher m = PATTERN_VARIABLES_BODY.matcher(variablesBody);
    LOG.debug("parsing variables body");
    while (m.find()) {
    	LOG.debug("found:" + m.group());
      final String key = m.group(1);
      final String value = m.group(2);
    	if (map.containsKey(key)) {
        LOG.warn("A duplicate variable name found with name: " + key + " and value: " + value + ".");
      }
      map.put(key, value);
    }
    return map;
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    final String css = IOUtils.toString(reader);
    final String result = parseCss(css);
    writer.write(result);
    writer.close();
  }

  /**
   * Parse css, find all defined variables & replace them.
   *
   * @param css to parse.
   */
  private String parseCss(final String css) {
    //map containing variables & their values
    final Map<String, String> map = new HashMap<String, String>();
    final StringBuffer sb = new StringBuffer();
    final Matcher m = PATTERN_VARIABLES_DEFINITION.matcher(css);
    while (m.find()) {
      final String variablesBody = m.group(1);
      //LOG.debug("variables body: " + variablesBody);
      //extract variables
      map.putAll(extractVariables(variablesBody));
      //remove variables definition
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);

    final String result = replaceVariables(sb.toString(), map);
    //LOG.debug("replaced variables: " + result);
    return result;
  }

  /**
   * Replace variables from css with provided variables map.
   *
   * @param cleanCss used to replace variable placeholders.
   * @param variables map of variables used for substitution.
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
