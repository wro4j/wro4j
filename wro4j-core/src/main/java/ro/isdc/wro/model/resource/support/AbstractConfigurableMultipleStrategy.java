package ro.isdc.wro.model.resource.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Abstracts the configurable creation of the strategies. Responsible for configuration of a list of strategies which
 * are configured as a CSV of aliases.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public abstract class AbstractConfigurableMultipleStrategy<S, P>
    extends AbstractConfigurableStrategySupport<S, P> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableMultipleStrategy.class);
  /**
   * Delimit tokens containing a list of locators, preProcessors & postProcessors.
   */
  private static final String TOKEN_DELIMITER = ",";

  /**
   * Creates a comma separated list of items.
   * 
   * @param items The items which should be combined into a string.
   * @return The string containing the concatenation of items.
   */
  public static String createItemsAsString(final String... items) {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < items.length; i++) {
      sb.append(items[i]);
      if (i < items.length - 1) {
        sb.append(TOKEN_DELIMITER);
      }
    }
    return sb.toString();
  }
  
  /**
   * @return the strategy S whose alias is found configured in the properties. This method will never return null. If an
   *         invalid alias is provided - a runtime exception will be thrown.
   */
  public final List<S> getConfiguredStrategies() {
    final List<S> strategies = new ArrayList<S>();
    final List<String> aliasList = getAliasList(getConfiguredValue());
    for (String alias : aliasList) {
      final S strategy = getStrategyForAlias(alias);
      if (strategy == null) {
        throw new WroRuntimeException("Invalid strategy alias provided: <" + alias + ">. Available aliases are: "
            + getAvailableAliases());
      }
      strategies.add(strategy);
    }
    LOG.debug("using strategies: {}", strategies);
    return strategies;
  }
  
  /**
   * Creates a list of aliases based on provided string containing comma separated values of aliases.
   * 
   * @param aliasCsv
   *          string representation of tokens separated by ',' character.
   * @return a list of non empty strings.
   */
  private List<String> getAliasList(final String aliasCsv) {
    LOG.debug("configured aliases: {}", aliasCsv);
    final List<String> list = new ArrayList<String>();
    if (!StringUtils.isEmpty(aliasCsv)) {
      final String[] tokens = aliasCsv.split(TOKEN_DELIMITER);
      for (final String token : tokens) {
        list.add(token.trim());
      }
    }
    return list;
  }
}
