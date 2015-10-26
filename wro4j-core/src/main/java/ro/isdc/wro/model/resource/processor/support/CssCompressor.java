/*
 * CSS Compressor
 * Author: Andy Roberts
 * Copyright (c) 2009, Andy Roberts. All rights reserved.
 *
 * This software is licensed under the BSD license.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of Andy Roberts nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission of Yahoo! Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * This code is a derivative of code distributed by Yahoo! Inc. Their rights
 * are fully acknowledged below:
 *
 * YUI Compressor
 * Author: Julien Lecomte <jlecomte@yahoo-inc.com>
 * Copyright (c) 2007, Yahoo! Inc. All rights reserved.
 * Code licensed under the BSD License:
 *     http://developer.yahoo.net/yui/license.txt
 *
 * This code is a port of Isaac Schlueter's cssmin utility.
 */
package ro.isdc.wro.model.resource.processor.support;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CssCompressor {

  private final StringBuffer srcsb = new StringBuffer();


  public CssCompressor(final Reader in) throws IOException {
    // Read the stream...
    int c;
    while ((c = in.read()) != -1) {
      srcsb.append((char)c);
    }
  }


  protected String mergeRules(final String inputCss) {

    final Map ruleMap = new LinkedHashMap();
    final StringBuffer mergedCss = new StringBuffer();

    final Pattern p = Pattern.compile("([^\\{]*)\\{(.*?)\\}");
    final Matcher m = p.matcher(inputCss);

    while (m.find()) {
      final String selectors = m.group(1);
      String rules = m.group(2);

      rules = compressDimensions(rules);

      if (ruleMap.containsKey(rules)) {
        ruleMap.put(rules, ruleMap.get(rules) + "," + selectors);
      } else {
        ruleMap.put(rules, selectors);
      }
    }

    String rule;
    for (final Iterator i = ruleMap.keySet().iterator(); i.hasNext();) {
      rule = (String)i.next();
      mergedCss.append(ruleMap.get(rule) + "{" + rule + "}");
    }

    return mergedCss.toString();
  }


  protected String removeDuplicateProperties(final String inputCssRule) {

    final StringBuffer cssRule = new StringBuffer();

    final Set ruleSet = new LinkedHashSet(Arrays.asList(inputCssRule.split(";")));

    for (final Iterator i = ruleSet.iterator(); i.hasNext();) {
      cssRule.append((String)i.next() + ";");
    }

    return cssRule.toString();
  }


  protected String compressDimensions(final String inputCssRule) {
    final Pattern p = Pattern.compile("(border|margin):(\\d+(?:\\p{Alpha}*))(\\2){3}");
    Matcher m;

    final StringBuffer cssRule = new StringBuffer();

    for (final String rule : inputCssRule.split(";")) {

      final String condensedRule = rule.replaceAll(" +", "");
      m = p.matcher(condensedRule);
      if (m.find()) {
        cssRule.append(condensedRule.substring(0, m.start()));
        cssRule.append(m.group(1) + ':' + m.group(2) + ';');
      } else {
        cssRule.append(rule + ';');
      }
    }

    return cssRule.toString();
  }


  public void compress(final Writer out, final int linebreakpos)
    throws IOException {

    Pattern p;
    Matcher m;
    String css;
    StringBuffer sb;
    int startIndex, endIndex;

    // Remove all comment blocks...
    startIndex = 0;
    boolean iemac = false;
    boolean preserve = false;
    sb = new StringBuffer(srcsb.toString());
    while ((startIndex = sb.indexOf("/*", startIndex)) >= 0) {
      preserve = sb.length() > startIndex + 2 && sb.charAt(startIndex + 2) == '!';
      endIndex = sb.indexOf("*/", startIndex + 2);
      if (endIndex < 0) {
        if (!preserve) {
          sb.delete(startIndex, sb.length());
        }
      } else if (endIndex >= startIndex + 2) {
        if (sb.charAt(endIndex - 1) == '\\') {
          // Looks like a comment to hide rules from IE Mac.
          // Leave this comment, and the following one, alone...
          startIndex = endIndex + 2;
          iemac = true;
        } else if (iemac) {
          startIndex = endIndex + 2;
          iemac = false;
        } else if (!preserve) {
          sb.delete(startIndex, endIndex + 2);
        } else {
          startIndex = endIndex + 2;
        }
      }
    }

    css = sb.toString();

    // Normalize all whitespace strings to single spaces. Easier to work with that way.
    css = css.replaceAll("\\s+", " ");

    // Make a pseudo class for the Box Model Hack
    css = css.replaceAll("\"\\\\\"}\\\\\"\"", "___PSEUDOCLASSBMH___");

    // Remove the spaces before the things that should not have spaces before them.
    // But, be careful not to turn "p :link {...}" into "p:link{...}"
    // Swap out any pseudo-class colons with the token, and then swap back.
    sb = new StringBuffer();
    p = Pattern.compile("(^|\\})(([^\\{:])+:)+([^\\{]*\\{)");
    m = p.matcher(css);
    while (m.find()) {
      String s = m.group();
      s = s.replaceAll(":", "___PSEUDOCLASSCOLON___");
      m.appendReplacement(sb, s);
    }
    m.appendTail(sb);
    css = sb.toString();
    css = css.replaceAll("\\s+([!{};:>+\\(\\)\\],])", "$1");
    css = css.replaceAll("___PSEUDOCLASSCOLON___", ":");

    // Remove the spaces after the things that should not have spaces after them.
    css = css.replaceAll("([!{}:;>+\\(\\[,])\\s+", "$1");

    // Add the semicolon where it's missing.
    css = css.replaceAll("([^;\\}])}", "$1;}");

    // Replace 0(px,em,%) with 0.
    css = css.replaceAll("([\\s:])(0)(px|em|%|in|cm|mm|pc|pt|ex)", "$1$2");

    // Replace 0 0 0 0; with 0.
    css = css.replaceAll(":0 0 0 0;", ":0;");
    css = css.replaceAll(":0 0 0;", ":0;");
    css = css.replaceAll(":0 0;", ":0;");
    // Replace background-position:0; with background-position:0 0;
    css = css.replaceAll("background-position:0;", "background-position:0 0;");

    // Replace 0.6 to .6, but only when preceded by : or a white-space
    css = css.replaceAll("(:|\\s)0+\\.(\\d+)", "$1.$2");

    // Shorten colors from rgb(51,102,153) to #336699
    // This makes it more likely that it'll get further compressed in the next step.
    p = Pattern.compile("rgb\\s*\\(\\s*([0-9,\\s]+)\\s*\\)");
    m = p.matcher(css);
    sb = new StringBuffer();
    while (m.find()) {
      final String[] rgbcolors = m.group(1).split(",");
      final StringBuffer hexcolor = new StringBuffer("#");
      for (final String rgbcolor : rgbcolors) {
        final int val = Integer.parseInt(rgbcolor);
        if (val < 16) {
          hexcolor.append("0");
        }
        hexcolor.append(Integer.toHexString(val));
      }
      m.appendReplacement(sb, hexcolor.toString());
    }
    m.appendTail(sb);
    css = sb.toString();

    // Shorten colors from #AABBCC to #ABC. Note that we want to make sure
    // the color is not preceded by either ", " or =. Indeed, the property
    // filter: chroma(color="#FFFFFF");
    // would become
    // filter: chroma(color="#FFF");
    // which makes the filter break in IE.
    p = Pattern.compile("([^\"'=\\s])(\\s*)#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
    m = p.matcher(css);
    sb = new StringBuffer();

    final Map colorMap = new LinkedHashMap();
    colorMap.put("C0C0C0", "silver");
    colorMap.put("800000", "maroon");
    colorMap.put("800080", "purple");
    colorMap.put("008000", "green");
    colorMap.put("808000", "olive");
    colorMap.put("000080", "navy");
    colorMap.put("008080", "teal");

    while (m.find()) {
      // Test for AABBCC pattern
      if (m.group(3).equalsIgnoreCase(m.group(4)) && m.group(5).equalsIgnoreCase(m.group(6))
        && m.group(7).equalsIgnoreCase(m.group(8))) {
        m.appendReplacement(sb, m.group(1) + m.group(2) + "#" + m.group(3) + m.group(5) + m.group(7));
      } else {
        // Test if hex code can be smaller as a named colour
        final String hex = m.group(3) + m.group(4) + m.group(5) + m.group(6) + m.group(7) + m.group(8);
        if (colorMap.containsKey(hex)) {
          m.appendReplacement(sb, m.group(1) + m.group(2) + colorMap.get(hex));
        } else {
          m.appendReplacement(sb, m.group());
        }
      }
    }
    m.appendTail(sb);
    css = sb.toString();

    // Replace named colors where they are shorter hex values
    //Comment since it has no effect (reported by find-bug)
//    css.replaceAll(":black", ":#000");
//    css.replaceAll(":white", ":#FFF");
//    css.replaceAll(":yellow", ":#FF0");

    // Remove empty rules.
    css = css.replaceAll("[^\\}]+\\{;\\}", "");

    if (linebreakpos >= 0) {
      // Some source control tools don't like it when files containing lines longer
      // than, say 8000 characters, are checked in. The linebreak option is used in
      // that case to split long lines after a specific column.
      int i = 0;
      int linestartpos = 0;
      sb = new StringBuffer(css);
      while (i < sb.length()) {
        final char c = sb.charAt(i++);
        if (c == '}' && i - linestartpos > linebreakpos) {
          sb.insert(i, '\n');
          linestartpos = i;
        }
      }

      css = sb.toString();
    }

    // Replace the pseudo class for the Box Model Hack
    css = css.replaceAll("___PSEUDOCLASSBMH___", "\"\\\\\"}\\\\\"\"");

    // Replace multiple semi-colons in a row by a single one
    // See SF bug #1980989
    css = css.replaceAll(";;+", ";");

    // Trim the final string (for any leading or trailing white spaces)
    css = css.trim();

    // Merge the classes
    css = mergeRules(css);

    // Remove the last semi-colon in blocks
    css = css.replaceAll(";\\}", "}");

    // Write the output...
    out.write(css);
  }
}