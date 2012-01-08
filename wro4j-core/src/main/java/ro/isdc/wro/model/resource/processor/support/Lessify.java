/*
 *  Copyright 2010 Richard Nichols.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Transforms colors from long format in a shorter one.
 *
 * @author Richard Nichols
 */
public class Lessify {
  private static final Pattern PATTERN_COLOR = Pattern.compile("#[0-9a-fA-F]{3,6}[^0-9a-fA-F]");
  private static final Pattern PATTERN_WORD_COLOR = Pattern.compile(getGiantCssColorRegex());


  public String variablizeColors(String css) {
    final Set<String> colors = new LinkedHashSet<String>();
    css = conformColors(css);
    final Matcher m = PATTERN_COLOR.matcher(css);
    final Map<String, Integer> colorCount = new HashMap<String, Integer>();
    while (m.find()) {
      final String color = m.group().substring(1, m.group().length() - 1);
      colors.add(color);
      if (colorCount.get(color) == null) {
        colorCount.put(color, 1);
      } else {
        colorCount.put(color, colorCount.get(color) + 1);
      }
    }
    final StringBuffer result = new StringBuffer();
    int idx = 0;
    final Map<String, String> colorMap = new HashMap<String, String>();
    for (final String color : colors) {
      colorMap.put(color, "color" + minLeading(idx, 3));
      idx++;
      result.append("@").append(colorMap.get(color)).append(": #").append(conformColor(color)).append("; /* used ").append(
        colorCount.get(color)).append(" times */\n");
    }
    for (final String color : colors) {
      css = css.replace("#" + color, "@" + colorMap.get(color));
    }
    result.append(css);
    return result.toString();
  }


  public String conformColors(String css) {
    Matcher m = PATTERN_COLOR.matcher(css);
    final Map<String, String> colGroup = new HashMap<String, String>();
    while (m.find()) {
      final String color = m.group().substring(1, m.group().length() - 1);
      colGroup.put(m.group(), conformColor(color));
    }
    m = PATTERN_WORD_COLOR.matcher(css);
    while (m.find()) {
      final String color = m.group().substring(1, m.group().length() - 1);
      colGroup.put(m.group(), "#" + conformColor(color));
    }
    // conform all
    for (final String col : colGroup.keySet()) {
      css = css.replace(col, col.charAt(0) + colGroup.get(col) + col.charAt(col.length() - 1));
    }
    return css;
  }


  private String conformColor(final String color) {
    if (CssColors.forName(color) != null) {
      return CssColors.forName(color).getColorAsHexString().toLowerCase();
    }
    if (color.length() == 3) {
      final StringBuffer sb = new StringBuffer();
      for (int n = 0; n < color.length(); n++) {
        sb.append(color.charAt(n));
        sb.append(color.charAt(n));
      }
      return sb.toString().toLowerCase();
    } else {
      return minLeading(color, 6).toLowerCase();
    }
  }


  private static String minLeading(final int val, final int min) {
    return minLeading(Integer.toString(val), min);
  }


  private static String minLeading(final String val, final int min) {
    final StringBuffer result = new StringBuffer(val);
    while (result.length() < min) {
      result.insert(0, "0");
    }
    return result.toString();
  }


  private static String getGiantCssColorRegex() {
    final StringBuffer sb = new StringBuffer("");
    boolean first = true;
    for (final CssColors col : CssColors.values()) {
      if (!first)
        sb.append("|");
      sb.append("(").append(buildRegexForCssColor(col)).append(")");
      first = false;
    }
    return sb.toString();
  }


  private static String buildRegexForCssColor(final CssColors col) {
    final StringBuffer sb = new StringBuffer("[\\s\\n\\r\\:]");
    for (int n = 0; n < col.name().length(); n++) {
      sb.append("[").append(Character.toLowerCase(col.name().charAt(n))).append(
        Character.toUpperCase(col.name().charAt(n))).append("]");
    }
    sb.append("[\\s\\n\\r;]");
    return sb.toString();
  }
}
