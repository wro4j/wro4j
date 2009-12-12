/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.algorithm;

/**
 * Copyright 2007 Jordi Hernández Sellés
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Minifies CSS files by removing expendable whitespace and comments.
 *
 * @author Jordi Hernández Sellés
 *
 */
public class JawrCssMinifier {

  // This regex captures comments
  private static final String commentRegex = "(/\\*[^*]*\\*+([^/][^*]*\\*+)*/)";

  // Captures CSS strings
  private static final String quotedContentRegex = "('(\\\\'|[^'])*')|(\"(\\\\\"|[^\"])*\")";

  // A placeholder string to replace and restore CSS strings
  private static final String STRING_PLACEHOLDER = "______'JAWR_STRING'______";

  // Captured CSS rules (requires replacing CSS strings with a placeholder, or quoted braces will fool it.
  private static final String rulesRegex = "([^\\{\\}]*)(\\{[^\\{\\}]*})";

  // Captures newlines and tabs
  private static final String newlinesTabsRegex = "\\r|\\n|\\t|\\f";

  // This regex captures, in order:
  // (\\s*\\{\\s*)|(\\s*\\}\\s*)|(\\s*\\(\\s*)|(\\s*;\\s*)|(\\s*\\))
  // brackets, parentheses,colons and semicolons and any spaces around them (except spaces AFTER a parentheses closing
  // symbol),
  // and ( +) occurrences of one or more spaces.
  private static final String spacesRegex = "(\\s*\\{\\s*)|(\\s*\\}\\s*)|(\\s*\\(\\s*)|(\\s*;\\s*)|(\\s*:\\s*)|(\\s*\\))|( +)";

  private static final Pattern commentsPattern = Pattern.compile(commentRegex, Pattern.DOTALL);
  private static final Pattern spacesPattern = Pattern.compile(spacesRegex, Pattern.DOTALL);

  private static final Pattern quotedContentPattern = Pattern.compile(quotedContentRegex, Pattern.DOTALL);
  private static final Pattern rulesPattern = Pattern.compile(rulesRegex, Pattern.DOTALL);
  private static final Pattern newlinesTabsPattern = Pattern.compile(newlinesTabsRegex, Pattern.DOTALL);
  private static final Pattern stringPlaceholderPattern = Pattern.compile(STRING_PLACEHOLDER, Pattern.DOTALL);

  private static final String SPACE = " ";
  private static final String BRACKET_OPEN = "{";
  private static final String BRACKET_CLOSE = "}";
  private static final String PAREN_OPEN = "(";
  private static final String PAREN_CLOSE = ")";

  private static final String COLON = ":";
  private static final String SEMICOLON = ";";

  /**
   * Template class to abstract the pattern of iterating over a Matcher and performing string replacement.
   */
  public abstract class MatcherProcessorCallback {
    String processWithMatcher(final Matcher matcher) {
      final StringBuffer sb = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(sb, matchCallback(matcher));
      }
      matcher.appendTail(sb);
      return sb.toString();
    }


    abstract String matchCallback(Matcher matcher);
  }


  /**
   * @param data CSS to minify
   * @return StringBuffer Minified CSS.
   */
  public StringBuffer minifyCSS(final StringBuffer data) {
    // Remove comments and carriage returns
    String compressed = commentsPattern.matcher(data.toString()).replaceAll("");

    // Temporarily replace the strings with a placeholder
    final List<String> strings = new ArrayList<String>();
    final Matcher stringMatcher = quotedContentPattern.matcher(compressed);
    compressed = new MatcherProcessorCallback() {
      @Override
      String matchCallback(final Matcher matcher) {
        final String match = matcher.group();
        strings.add(match);
        return STRING_PLACEHOLDER;
      }
    }.processWithMatcher(stringMatcher);

    // Grab all rules and replace whitespace in selectors
    final Matcher rulesmatcher = rulesPattern.matcher(compressed);
    compressed = new MatcherProcessorCallback() {
      @Override
      String matchCallback(final Matcher matcher) {
        final String match = matcher.group(1);
        final String spaced = newlinesTabsPattern.matcher(match.toString()).replaceAll(SPACE).trim();
        return spaced + matcher.group(2);
      }
    }.processWithMatcher(rulesmatcher);

    // Replace all linefeeds and tabs
    compressed = newlinesTabsPattern.matcher(compressed).replaceAll("");

    // Match all empty space we can minify
    final Matcher matcher = spacesPattern.matcher(compressed);
    compressed = new MatcherProcessorCallback() {
      @Override
      String matchCallback(final Matcher matcher) {
        String replacement = SPACE;
        final String match = matcher.group();

        if (match.indexOf(BRACKET_OPEN) != -1)
          replacement = BRACKET_OPEN;
        else if (match.indexOf(BRACKET_CLOSE) != -1)
          replacement = BRACKET_CLOSE;
        else if (match.indexOf(PAREN_OPEN) != -1)
          replacement = PAREN_OPEN;
        else if (match.indexOf(COLON) != -1)
          replacement = COLON;
        else if (match.indexOf(SEMICOLON) != -1)
          replacement = SEMICOLON;
        else if (match.indexOf(PAREN_CLOSE) != -1)
          replacement = PAREN_CLOSE;

        return replacement;
      }
    }.processWithMatcher(matcher);

    // Restore all Strings
    final Matcher restoreMatcher = stringPlaceholderPattern.matcher(compressed);
    final Iterator<String> it = strings.iterator();
    compressed = new MatcherProcessorCallback() {
      @Override
      String matchCallback(final Matcher matcher) {
//        return it.next();
         final String replacement = it.next();
         return adaptReplacementToMatcher(replacement);
      }
    }.processWithMatcher(restoreMatcher);
    return new StringBuffer(compressed);
  }


  /**
   * Fixes a bad problem with regular expression replacement strings. Replaces \ and $ for escaped versions for regex
   * replacement. This was somehow fixed in java 5 (a similar method was added). Since Jawr supports 1.4, this method is
   * used instead.
   *
   * @param replacement
   * @return
   */
  public static String adaptReplacementToMatcher(final String replacement) {
    // Double the backslashes, so they are left as they are after replacement.
    String result = replacement.replaceAll("\\\\", "\\\\\\\\");
    // Add backslashes after dollar signs
    result = result.replaceAll("\\$", "\\\\\\$");
    return result;
  }

}
