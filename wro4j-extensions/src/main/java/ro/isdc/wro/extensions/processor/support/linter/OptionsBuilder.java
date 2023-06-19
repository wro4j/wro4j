/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Responsible for building json representations of the options consumed by linter code.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public class OptionsBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(OptionsBuilder.class);
  /**
   * Split multiple options into an array of options.
   *
   * @param optionAsString string representation of multiple options.
   * @return an array of options.
   */
  public String[] splitOptions(final String optionAsString) {
    return optionAsString == null ? ArrayUtils.EMPTY_STRING_ARRAY : optionAsString.split("(?ims),(?![^\\[\\]]*\\])");
  }

  /**
   * Builds options json representation from a set of options encoded in a string, each of them being separated by a
   * comma.
   *
   * @param optionsAsCsv
   * @return json representation of options.
   */
  public String buildFromCsv(final String optionsAsCsv) {
    return build(splitOptions(optionsAsCsv));
  }

  /**
   * @param options
   *          an array of options as provided by user.
   * @return the json object containing options to be used by linter code.
   */
  public String build(final String... options) {
    LOG.debug("options: {}", Arrays.toString(options));
    final StringBuffer sb = new StringBuffer("{");
    if (options != null) {
      for (int i = 0; i < options.length; i++) {
        final String option = options[i];
        if (!StringUtils.isEmpty(option)) {
          sb.append(processSingleOption(option));
          if (i < options.length - 1) {
            sb.append(",");
          }
        }
      }
    }
    sb.append("}");
    LOG.debug("options: {}", sb);
    return sb.toString();
  }

  private String processSingleOption(final String option) {
    String optionName = option;
    String optionValue = Boolean.TRUE.toString();
    if (option.contains(":")) {
      final String message = String.format("Invalid option: '%s'. Expected format: key=value or key=['v1','v2']",
          option);
      LOG.error(message);
      throw new WroRuntimeException(message);
    }
    if (option.contains("=")) {
      final String[] optionEntry = option.split("=");
      if (optionEntry.length != 2) {
        throw new IllegalArgumentException("Invalid option provided: " + option);
      }
      optionName = optionEntry[0];
      optionValue = option.split("=")[1];
    }
    return String.format("\"%s\": %s", optionName.trim(), optionValue.trim());
  }

}
