/**
 * Copyright (c) 2009 wro4j. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.processor.ResourcePreProcessor;


/**
 * Preprocessor of css resources, responsible for replacing variables. (@see
 * http://disruptive-innovations.com/zoo/cssvariables/). This is a pre processor, because it makes sense to apply
 * variables only on the same css. <br/>
 * TODO: check if it makes sense to make it post processor instead.
 *
 * @author alexandru.objelean
 * @created Created on Jul 05, 2009
 */
public class CssVariablesPreprocessor
  implements ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(CssVariablesPreprocessor.class);
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
  private static final String REGEX_VARIABLES_BODY = "(\\w+):\\s*(.+?);";
  /**
   * Pattern used to parse variables body & to extract mapping between variable & its value. For instance:<br/>
   * <code>
   *   var(corporateLogo);
   * </code>
   */
  private static final String REGEX_VARIABLE_HOLDER = "var\\s*\\((.+?)\\);";

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
   * Removes variables definition.
   * @param css from where to remove variables definitions.
   * @return css content without variables definition.
   */
  private String cleanUp(final String css) {
    return css;
  }

  /**
   * Parse css & find all defined variables.
   * @param css to parse.
   * @return a map of variables & their values.
   */
  private Map<String, String> findVariables(final String css) {
    final Map<String, String> map = new HashMap<String, String>();

    final Matcher m = PATTERN_VARIABLES_DEFINITION.matcher(css);
    while (m.find()) {
      final String variablesBody = m.group(1);
      log.debug("variables body: " + variablesBody);
      map.putAll(extractVariables(variablesBody));
    }
    log.debug("extracted map: " + map);
    return map;
  }

  /**
   * Extract variables map from variables body.
   * @param variablesBody string containing variables mappings.
   * @return map with extracted variables.
   */
  private Map<String, String> extractVariables(final String variablesBody) {
    final Map<String, String> map = new HashMap<String, String>();
    final Matcher m = PATTERN_VARIABLES_BODY.matcher(variablesBody);
    log.debug("parsing variables body");
    while (m.find()) {
      map.put(m.group(1), m.group(2));
    }
    return map;
  }

  /**
   * @param css
   * @return
   */
  private String parseCss(final String css) {
    //find variables
    final Map<String, String> variables = findVariables(css);
    //skip processing if no variables found
    if (variables.isEmpty()) {
      return css;
    }
    //cleanUp - remove variables definition
    final String cleanCss = cleanUp(css);
    //replace variables
    final String result = replaceVariables(cleanCss, variables);
    log.debug("replaced variables: " + result);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader, final Writer writer)
    throws IOException {
    final String css = IOUtils.toString(reader);
    final String result = parseCss(css);
    writer.write(result);
    writer.close();
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
      final String newReplacement = oldMatch.replace(oldMatch, variables.get(variableName));
      m.appendReplacement(sb, newReplacement + ";");
    }
    m.appendTail(sb);
    return sb.toString();
  }

}
